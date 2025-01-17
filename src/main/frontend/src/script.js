
import React, {useEffect, useRef, useState} from "react";
import axios from "axios";
import "./script.css";
import {Link, useNavigate} from "react-router-dom";
import ChatPage from "./chatroom";
import chatImage from './images/chat.svg';
import {domain} from "./domain";
import { useWebSocket } from './WebsocketContext';
import mainlogo from "./images/mainlogo.png";
import backlink from "./images/back.svg";
import Online from "./online"; // WebSocketProvider의 훅 사용


const Script = () => {
  const [slides, setSlides] = useState([]);
  const [chatModal, setChatModal] = useState(false);
  const {stompClient, isConnected, roomId, userId, leaderId, presentationId, online} = useWebSocket();
  const subscriptions = useRef([]); // 구독후 반환하는 객체로, 해당 객체로 구독을 취소해야 한다.
  const navigate = useNavigate();
  const [pptPath, setPptPath] = useState('');
  const [newScripts, setNewScripts] = useState({});
  const [currentPage, setCurrentPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [checkSave, setCheckSave] = useState(false);

    // 발표자료의 슬라이드를 가져오는 함수
    const fetchSlides = () => {
        axios.get(`${domain}/api/v1/presentation/${presentationId}/slides`)
            .then((res) => {
                setSlides(res.data.data.slides);
                setIsLoading(false);
            })
            .catch((e) => {
                alert("슬라이드를 가져오는 데 실패하였습니다!")
                console.log(e);
            });
        axios.get(`${domain}/api/v1/rooms/${roomId}/presentation`)
            .then((res)=>{
                setPptPath(res.data.data.presentationPath);
            })
    }

    //=============================================웹소켓========================================================
    const receiveMessage = (message) => {
        //3-1 구독한 url에서 온 메세지를 받았을 때
        const frame = JSON.parse(message.body)
        if (frame.messageType === "SCRIPT_UPDATE") {
            updateScriptInScreen(frame.data)
        } else {
            console.log("Not Supported Message Type")
        }
    }

    const receiveError = (error) => {
        //3-2 구독한 url에서 온 메세지를 못 받아 에러가 발생했을 때
        console.error("STOMP Error", error);
        window.location.href = "/";
    }

    const onConnect = () => {
        //2-1 연결 성공의 경우
        if(presentationId){
            fetchSlides();
        }
        subscriptions.current = stompClient.current.subscribe(
            `/sub/rooms/${roomId}/scripts`,
            receiveMessage,
            receiveError
        );
    }

    useEffect(() => {
        //1. broker endPoint에 연결, WebsocketConfig에 설정한 EndPoint를 말함
        if (stompClient.current) {
            stompClient.current.activate(); // 웹소켓 활성화
        }

        return () => {
            if (stompClient.current) {
                subscriptions.current.unsubscribe();
            }
        };
    }, [roomId]);

    useEffect(() => {
        if (isConnected) {
            onConnect(); // 연결이 완료되면 onConnect 호출
        }
    }, [isConnected]); //isConnected 상태가 바뀌면 실행된다.
    //===============================================================================
   //===================================스크립트 등록===================================
  const addScript = (scriptId, script) => {
      const data = {
          roomId,
          scriptId,
          script,
      }
          stompClient.current.publish({
          destination: '/pub/scripts/update',
          body: JSON.stringify(data)
      });
      setSlides((prevSlides) =>
          prevSlides.map((slide) =>
              slide.scriptId === scriptId
                  ? { ...slide, script } // 업데이트된 스크립트 반영
                  : slide
          )
      );
      setCheckSave(true);
      console.log('checked');
      setTimeout(() => {
          console.log('check out');
          setCheckSave(false);
      }, 2000);
  };

  const updateScriptInScreen = (frame) => {
      setSlides(prevSlides =>
          prevSlides.map(slide =>
              slide.scriptId === frame.scriptId
                  ? { ...slide, script: frame.script }
                  : slide
          )
      );

  }


    const handleScriptChange = (slideId, value) => {
        setNewScripts((prevScripts) => ({
            ...prevScripts,
            [slideId]: value, // slideId에 해당하는 상태만 업데이트
        }));
  };
  //========================================슬라이드 새로고침======================================
    const refreshSlides = () => {
        setIsLoading(true);
        axios.post(`${domain}/api/v1/presentation/${presentationId}/slides-compare`)
            .then(()=>{
                fetchSlides();
            })
    }
  //===========================================기타 기능===========================================
    const goSection = (path, subUrl) => {
        const state = {
            roomId,
            subUrl: subUrl,
            userId,
            leaderId,
        };
        if (presentationId != null) {
            state.presentationId = presentationId;
        }
        navigate(path, {state})
    }

    //채팅창 토글로 구현
    const toggleChatModal = () => {
        setChatModal((prevState) => !prevState);
    };

    //뒤로가기
    const goBack = () => {
        const state = {};
        if (presentationId != null) {
            state.presentationId = presentationId;
        }
        navigate("/project", {state}); // "/project" 경로로 이동
    };

    //슬라이드 페이지화

    const goToNextPage = () => {
        if (currentPage < slides.length - 1) {
            setCurrentPage(currentPage + 1);
        }
    };

    const goToPrevPage = () => {
        if (currentPage > 0) {
            setCurrentPage(currentPage - 1);
        }
    };

    const goToFirstPage = () => {
        setCurrentPage(0);
    };

    const goToLastPage = () => {
        setCurrentPage(slides.length - 1);
    };

    const handlePageSelectChange = (e) => {
        setCurrentPage(Number(e.target.value)); // 선택된 페이지로 이동
    };
    //===============================================================================

  return (
      <div className="background">
          <Online online={online}/>
          <img src={mainlogo} className="upper-logo"/>
          <button onClick={goBack} className="back_link">
              <img src={backlink}/>
          </button>
          <div className="slide-view-container">
              <div className="slide-content">
                  <div className="slides-preview">
                      {slides.length > 0 ? (
                          <div key={slides[currentPage].slideId} className="slide-item">
                              {/* 썸네일 - 왼쪽 */}
                              <div
                                  className="slide-thumbnail"
                                  onClick={() =>
                                      window.open(
                                          `https://docs.google.com/presentation/d/${pptPath}/edit#slide=${slides[currentPage].slideUrl}`,
                                          "_blank"
                                      )
                                  }
                                  style={{cursor: "pointer"}}
                              >
                                  <img
                                      src={`${domain}/proxy/thumbnail?url=${encodeURIComponent(slides[currentPage].thumbnailUrl)}`}
                                      alt={`Slide ${slides[currentPage].slideId}`}
                                  />
                              </div>

                              {/* 스크립트 - 오른쪽 */}
                              <div className="slide-script">
                                  <h4>슬라이드 {currentPage + 1}</h4>
                                  <textarea
                                      className="script-textarea"
                                      value={newScripts[slides[currentPage].slideId] ??
                                          slides[currentPage].script ?? ""}
                                      onChange={(e) =>
                                          handleScriptChange(
                                              slides[currentPage].slideId,
                                              e.target.value
                                          )
                                      }
                                      placeholder={
                                         "스크립트를 입력하세요"
                                      }
                                      rows="5"
                                  />
                                  <button
                                      onClick={() =>
                                          addScript(
                                              slides[currentPage].scriptId,
                                              newScripts[slides[currentPage].slideId]
                                          )
                                      }
                                      className="save-btn"
                                  >
                                      저장
                                  {checkSave && (
                                      <div className="save-toast">
                                          저장 완료!
                                      </div>)}
                                  </button>
                              </div>
                          </div>
                      ) : (
                          <p>슬라이드가 없습니다.</p>
                      )}

                      {/* 페이지 전환 버튼 */}
                      <div className="pagination-buttons">
                          <button onClick={goToFirstPage} disabled={currentPage === 0}>
                              &lt;&lt; {/* 첫 번째 페이지로 */}
                          </button>
                          <button onClick={goToPrevPage} disabled={currentPage === 0}>
                              &lt; {/* 이전 페이지로 */}
                          </button>

                          {/* 페이지 번호 선택 드롭다운 */}
                          <div className="page-select">
                              <select
                                  value={currentPage}
                                  onChange={handlePageSelectChange}
                              >
                                  {Array.from({length: slides.length}, (_, index) => (
                                      <option key={index} value={index}>
                                          {index + 1}
                                      </option>
                                  ))}
                              </select>
                          </div>

                          <button onClick={goToNextPage} disabled={currentPage === slides.length - 1}>
                              &gt; {/* 다음 페이지로 */}
                          </button>
                          <button onClick={goToLastPage} disabled={currentPage === slides.length - 1}>
                              &gt;&gt; {/* 마지막 페이지로 */}
                          </button>
                      </div>
                      {/* 새로고침 버튼 */}
                      {presentationId && (!isLoading ?
                          <button className="refresh-btn" onClick={refreshSlides}>🔄</button>
                          :<div className="script-spinner"></div> )}
                  </div>
              </div>
          </div>
          <div>
              <button className="chat-button" onClick={toggleChatModal}>
                  <img className="chat_image" src={chatImage} alt="채팅창 이미지"/>
              </button>
              <div className={`chat-modal ${chatModal ? 'open' : ''}`}>
                  {chatModal && <ChatPage/>}
              </div>
          </div>


          <div className="process">
              <div onClick={() => goSection('/topic', `/sub/rooms/${roomId}/topics`)}>
                  주제 선정
              </div>
              <div onClick={() => goSection('/part', `/sub/rooms/${roomId}/parts`)}>
                  자료 조사
              </div>
              <div onClick={() => goSection('/presentation', `/sub/rooms/${roomId}/presentation`)}>
                  발표 자료
              </div>
              <div className="active" onClick={() => goSection('/script', `/sub/rooms/${roomId}/scripts`)}>
                  발표 준비
              </div>
          </div>
      </div>
  );
};

export default Script;