
import React, {useEffect, useRef, useState} from 'react';
import axios from 'axios';
import { useNavigate } from "react-router-dom";
import { useWebSocket } from './WebsocketContext'; // WebSocketProvider의 훅 사용
import './ppt.css';
import ChatPage from "./chatroom";
import chatImage from './images/chat.svg';
import pptImage from './images/ppt.svg';
import {domain} from "./domain";
import mainlogo from "./images/mainlogo.png";
import backlink from "./images/back.svg";
import Online from "./online";

const PPT = () => {
    const [pptModal, setPPTModal] = useState(false); // ppt 생성 클릭 시 나오는 모달
    const [pptData, setPPTData] = useState(null); // get한 ppt 데이터 저장 공간
    const [editModal, setEditModal] = useState(false); // ppt 수정 모달
    const [chatModal, setChatModal] = useState(false);
    const {stompClient, isConnected, roomId, userId, leaderId, presentationId, online} = useWebSocket();
    const subscriptions = useRef([]); // 구독후 반환하는 객체로, 해당 객체로 구독을 취소해야 한다.
    const navigate = useNavigate();
    const [isValid, setIsValid] = useState(false);
    const [newPPTName, setNewPPTName] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const isLeader = (userId === leaderId);
    const [errorMessage, setErrorMessage] = useState('');
    const [newPath, setNewPath] = useState('');
    const [newPPT, setNewPPT] = useState(null);
    const [mainThumbnail, setMainThumbnail] = useState(null);
    const [createResult, setCreateResult] = useState(false);
    // 방의 PPT를 가져오는 함수
    const fetchPPT = () => {
        axios.get(`${domain}/api/v1/rooms/${roomId}/presentation`)
            .then((res) => {
                setPPTData(res.data.data);
            })
            .catch((error) => {
                if(error.response){
                    if(error.response.status === 400){
                        setPPTData(null);
                    }
                }
            });
    }

    const fetchMainThumbnail = () => {
        axios.get(`${domain}/api/v1/presentation/${pptData.presentationId}/first-page`)
            .then((res)=>{
                setMainThumbnail(res.data.data);
            })
    }

    useEffect(() => {
        if(pptData) {
            fetchMainThumbnail();
        }
    }, [pptData]);

    //=============================================웹소켓========================================================
    const receiveMessage = (message) => {
        //3-1 구독한 url에서 온 메세지를 받았을 때
        const frame = JSON.parse(message.body)
        if (frame.messageType === "PRESENTATION_UPDATE") {
            setIsLoading(false);
            updatePPTInScreen(frame.data);
        } else if (frame.messageType === "PRESENTATION_CREATE") {
            setIsLoading(false);
            createPPTInScreen(frame.data);
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
        fetchPPT();
        subscriptions.current = stompClient.current.subscribe(
            `/sub/rooms/${roomId}/presentation`,
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
    //======================================PPT 생성 ====================================
    // PPT 생성 클릭
    const createPPT =  () => {
        setIsLoading(true);
        setPPTModal(false);
        setNewPPTName('');
        const data = {
            roomId,
            presentationName: newPPTName
        }
        stompClient.current.publish({
            destination: '/pub/presentation/create',
            body: JSON.stringify(data)
        });
    };

    // 생성된 PPT 반영
    const createPPTInScreen = (frame) => {
        setPPTData(frame);
    }

    const closePPTModal = () => {
        setPPTModal(false);
        setNewPPTName('');
    }
    //==================================PPT 업데이트========================================
    // 수정 버튼 클릭 시
    const updatePPTInScreen = (frame) => {
        setPPTData(frame);
    }

    const checkValidPPT = (newPath) => {
        axios.get(`${domain}/api/v1/checkValidPPT/${newPath}`)
            .then(()=>{
                setNewPath(newPath);
                setIsValid(true);
            })
            .catch(()=>{
                setIsLoading(false);
                setErrorMessage("존재하지 않는 슬라이드입니다. 다시 확인해주세요");
            })
    }
    const editPPT = () => {
        setIsLoading(true);
        const newPath = newPPTName.split('/d/')[1]?.split('/')[0];
        if(newPath) {
            checkValidPPT(newPath);
        }
        else{
            setIsLoading(false);
            setErrorMessage("슬라이드 url을 전부 복사해주세요");
        }
    };

    const syncPPT= () => {
        setIsLoading(true);
        axios.post(domain+`/api/v1/presentation/${pptData.presentationId}/slides-sync`)
            .then(()=>{
                fetchMainThumbnail();
                setIsLoading(false);
            })
            .catch((e)=>{
                setIsLoading(false);
                alert(e.message);
            })
    }

    useEffect(() => {
        if(isValid) {
            const payload = {
                roomId,
                presentationPath: newPath,
            };
            stompClient.current.publish({
                destination: "/pub/presentation/update",
                body: JSON.stringify(payload),
            });
            closeEditModal();
        }
        setIsValid(false);
    }, [isValid]);

    const closeEditModal = () => {
        setErrorMessage('');
        setEditModal(false);
        setNewPPTName('');
        setNewPath('');
    }

    //================================PPT 다운로드===================================
    const downloadPDF = () => {
        const pdfURL = `${domain}/api/v1/presentation/${pptData.presentationId}/exportPdf`;
        window.location.href = pdfURL;
    }

    const downloadPPT = () => {
        const pptURL = `${domain}/api/v1/presentation/${pptData.presentationId}/exportPptx`;
        window.location.href = pptURL;
    }
    //==============================================================================
    //뒤로가기
    const goBack = () => {
        const state = {};
        if (presentationId != null) {
            state.presentationId = presentationId;
        }
        navigate("/project", {state}); // "/project" 경로로 이동
    };

    //채팅창 토글로 구현
    const toggleChatModal = () => {
        setChatModal((prevState) => !prevState);
    };

    const handleCreateKeyPress = (e) => {
        if (e.key === "Enter") {
            createPPT();
        }
    };

    const handleEditKeyPress = (e) => {
        if (e.key === "Enter") {
            editPPT();
        }
    }

    const goSection = (path, subUrl) => {
        const state = {
            roomId,
            subUrl: subUrl,
            userId,
            leaderId,
        };
        if(pptData != null) {
            state.presentationId = pptData.presentationId;
        }
        navigate(path, {state})

    }

    useEffect(() => {
        fetchPPT();
    }, []);


    return (
        <div className="background">
            <Online online={online}/>
            <img src={mainlogo} className="upper-logo"/>
            <button onClick={goBack} className="back_link">
                <img src={backlink}/>
            </button>
            <div className="ppt-container">
                {!pptData ? (
                    <div className="no-ppt-container">
                        <img src={pptImage} className="empty-thumbnail" alt="빈 썸네일"/>
                        <p className="no-ppt-text">
                            팀장을 통해 새로운 발표자료를 생성해 보세요!<br/>
                            생성된 발표자료는 미리보기 클릭 후 구글 드라이브에서 확인할 수 있습니다.
                        </p>
                        {isLeader && (
                            <div className="button-group">
                                <button className="create-ppt-btn" onClick={() => setPPTModal(true)}>새 슬라이드 생성</button>
                                <button className="edit-ppt-btn" onClick={() => setEditModal(true)}>기존 슬라이드 등록</button>
                            </div>
                        )}
                    </div>
                ) : (mainThumbnail &&
                    <div className="ppt-content">
                        <img
                            src={`${domain}/proxy/thumbnail?url=${encodeURIComponent(mainThumbnail)}`}
                            alt="PPT 썸네일"
                            className="main-thumbnail"
                            onClick={() => {
                                const slideUrl = `https://docs.google.com/presentation/d/${pptData.presentationPath}`;
                                window.open(slideUrl, "_blank");
                            }}
                        />
                            {!isLeader &&
                                <div className="bookmark-buttons">
                                    <button className="download-ppt-btn" onClick={downloadPPT}>PPT로 다운로드</button>
                                    <button className="download-pdf-btn" onClick={downloadPDF}>PDF로 다운로드</button>
                                    <button className="sync-ppt-btn" onClick={syncPPT}>썸네일 새로고침</button>
                                </div>
                            }
                        {isLeader && (
                            <div className="bookmark-buttons-leader">
                                    <button className="download-pdf-btn" onClick={downloadPDF}>PDF로 다운로드</button>
                                    <button className="download-ppt-btn" onClick={downloadPPT}>PPT로 다운로드</button>
                                    <button className="sync-ppt-btn" onClick={syncPPT}>썸네일 새로고침</button>
                                    <button className="create-ppt-after-btn" onClick={() => setPPTModal(true)}>새 슬라이드 생성
                                    </button>
                                    <button className="edit-ppt-after-btn" onClick={() => setEditModal(true)}>기존 슬라이드 등록
                                    </button>
                                </div>
                            )}
                    </div>
                )}
            </div>

            {isLoading && (
            <div className="loading-overlay">
                    <div className="spinner"></div>
                    <p>Loading...</p>
                </div>
            )}

            {pptModal && (
                <div className="ppt-modal-overlay" onClick={closePPTModal}>
                    <div className="ppt-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>PPT 생성</h2>
                        <p>생성 할 ppt 제목을 아래에 작성해주세요.</p>
                        <input className="ppt-input"
                               type="text"
                               value={newPPTName}
                               onKeyPress={handleCreateKeyPress}
                               onChange={(e) => setNewPPTName(e.target.value)}
                        />
                        <button className="register-btn" onClick={createPPT}>
                            생성
                        </button>
                        <button className="close-modal-btn" onClick={closePPTModal}>
                            X
                        </button>
                    </div>
                </div>
            )}

            {editModal && (
                <div className="ppt-modal-overlay" onClick={closeEditModal}>
                    <div className="ppt-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>PPT 등록</h2>
                        <p>등록 할 슬라이드 url을 아래에 붙여넣기 해주세요.</p>
                        <input className="ppt-input"
                               type="text"
                               value={newPPTName}
                               onKeyPress={handleEditKeyPress}
                               onChange={(e) => setNewPPTName(e.target.value)}
                        />
                        {errorMessage && (
                            <p className="error-message">{errorMessage}</p>
                        )}
                        <button className="register-btn" onClick={editPPT}>
                            등록
                        </button>
                        <button className="close-modal-btn" onClick={closeEditModal}>
                            X
                        </button>
                    </div>
                </div>
            )}
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
                <div className="active" onClick={() => goSection('/presentation', `/sub/rooms/${roomId}/presentation`)}>
                    발표 자료
                </div>
                <div onClick={() => goSection('/script', `/sub/rooms/${roomId}/scripts`)}>
                    발표 준비
                </div>
            </div>
        </div>
    );
};

export default PPT;
