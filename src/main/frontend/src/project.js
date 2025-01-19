import React, { useEffect, useState } from 'react';
import {Link, useLocation} from 'react-router-dom';
import axios from "axios";
import userImage from "./images/user.svg"
import "./project.css";
import Footer from "./footer";
import {domain} from "./domain";
import "./scrollbar.css"
import "./card.css"
import "./customModal.css"
import "./paginationButton.css"
import "./buttons.css"
import { useNavigate } from 'react-router-dom';
import "./juaFont.css"
import searchIcon from "./images/search.svg";
import emptyBox from "./images/emptyBox.svg"
import emptyProject from "./images/project.svg"
import mainlogo from "./images/mainlogo.png";
import {createFuzzyMatcher} from "./searchRexp";

const Project = () => {
  const [createmodal, setCreateModal] = useState(false);
  const [notJoinedRooms, setNotJoinedRooms] = useState({num: 0, rooms: []});
  const [allResult, setAllResult] = useState({num: 0, members: []}); // 초대할 친구 정보
  const [viewResult, setViewResult] = useState({num : 0, members : []})
  const [participant, setParticipant] = useState({num: 0, members: []}); // 이미 초대된 친구들 정보

  const [enterRoomId, setEnterRoomId] = useState(0)
  const [page, setPage] = useState(0);
  const [searchModal, setSearchModal] = useState(false);
  const [enterModal, setEnterModal] = useState(false);
  const [friendModal, setFriendModal] = useState(false);
  const [debouncedSearchFriendName, setDebouncedSearchFriendName] = useState(null);
  const navigate = useNavigate();
  const [enterRoomTitle, setEnterRoomTitle] = useState("");
  const [userId, setUserId] = useState(null);
  const location = useLocation();
  const {presentationId} = location.state || {};
  const [searchFriendName, setSearchFriendName] = useState("")

  const [notJoinedRoomPage, setNotJoinedRoomPage] = useState(1)

  // ====================================================
  const [rooms, setRooms] = useState({num: 0, roomList: []});
  const [currentPage, setCurrentPage] = useState(1); // 현재 페이지 상태 추가
  const [isLoading, setIsLoading] = useState(false);
  const [searchRoomsTitle , setSearchRoomsTitle] = useState("")
  const [searchExcludedRoomsTitle , setSearchExcludedRoomsTitle] = useState("")
  const [debouncedSearchRoomsTitle, setDebouncedSearchRoomsTitle] = useState("");

  useEffect(() => {
      const handler = setTimeout(() => {
          setDebouncedSearchRoomsTitle(searchRoomsTitle);
      }, 500); // 1초 대기

      return () => clearTimeout(handler); // 이전 타이머를 정리
  }, [searchRoomsTitle]);

  useEffect(() => {
      setParticipationRoom(0); // 검색 실행
  }, [debouncedSearchRoomsTitle]);


    // 방 목록 가져오기 (페이지에 따라 호출)
  const fetchRooms = (page,searchTitle,isParticipation) => {
      return axios
          .get(domain + `/api/v2/rooms?isParticipation=${isParticipation}&title=${searchTitle}&page=${page}`)
          .then((res) => {
              return res.data.data
          })
          .catch(() => {
              console.log("Failed to load rooms");
              return undefined
          });
  };

  const setParticipationRoom = (page) => {
      const searchTitle = searchRoomsTitle !== undefined ? searchRoomsTitle : ""
      fetchRooms(page,searchRoomsTitle,true)
          .then((result) => {
              setCurrentPage(page)
              setRooms(result)
          })
          .catch((error) => {
              console.error("Error fetching results:", error);
          });
  }

  const setNonParticipationRoom = (page) => {
      if(!searchExcludedRoomsTitle || searchExcludedRoomsTitle === undefined){
          return
      }
      fetchRooms(page,searchExcludedRoomsTitle,false)
          .then((result) => {
              setNotJoinedRoomPage(page)
              setNotJoinedRooms(result)
          })
          .catch((error) => {
              console.error("Error fetching results:", error);
          });
  }

  const deleteRoom = async (roomId) => {
      try {
          setIsLoading(true); // 로딩 시작
          await axios.delete(`${domain}/api/v1/rooms`, {
              data: {
                  roomId,
              },
          });
          fetchRooms(currentPage); // 방 목록 다시 불러오기
      } catch (error) {
          alert("프로젝트를 삭제하지 못했습니다.");
          console.error("Failed to delete room:", error);
      } finally {
          setIsLoading(false); // 로딩 종료
      }
  };

  // ====================================================
  useEffect(() => {
      const timer = setTimeout(() =>
        setDebouncedSearchFriendName(searchFriendName), 500);
      return () => clearTimeout(timer);
  }, [searchFriendName]);

  useEffect(() => {
      if (debouncedSearchFriendName) {
          //검색어가 존재할 경우
          viewJoinFriends(debouncedSearchFriendName);
      } else {
          //검색어가 없을 경우 결과 초기화
          setViewResult(allResult);
      }
  }, [debouncedSearchFriendName]);

    //마운트 할 때 유저id 들고오기
  useEffect(() => {
      setParticipationRoom(0);
      userFetch();
  }, []);

    //유저 id 들고오기(소켓에서 활용)
    const userFetch = async () => {
        try {
            const res = await axios.get(`${domain}/api/user-info`);
            setUserId(res.data);
        } catch (error) {
            console.error("유저 정보를 가져오는 데 실패했습니다.", error);
        }
    };

    const handleSearch = ({page}) => {
        const searchTitle = document.getElementById("roomSearchInput").value;

        axios
            .get(`${domain}/api/v1/rooms/search?title=${searchTitle}&page=${page}`)
            .then((res) => {
                setNotJoinedRooms(res.data.data);
            })
            .catch(() => {
                console.log("Failed to search project.");
            });
    };

    const enterRoom = (roomId, roomTitle) =>{
        setEnterRoomId(roomId)
        setEnterRoomTitle(roomTitle)
        setEnterModal(true)
    }
    const verifyPasswordAndEnterRoom = (roomId) =>{
        const password = document.getElementById("roomPasswordInput").value
        const data = {
            roomId,
            password
        }
        axios
            .post(`${domain}/api/v1/rooms/enter-room`,data,{ "Content-Type": "application/json"})
            .then((res) =>{
                const leaderId = res.data.data.leaderId
                if(leaderId){
                    const state = {
                        roomId,
                        subUrl: `/sub/rooms/${roomId}/topics`,
                        userId,
                        leaderId
                    };
                    if(presentationId!=null){
                        state.presentationId = presentationId;
                    }
                    //비밀 번호가 맞다면, 방을 입장
                    navigate('/topic', {state});
                    closeEnterModal()
                }
            })
            .catch((error) =>{
                if (error.response) {
                    const passwordInvalidDiv = document.getElementById("passwordInvalidDiv");
                    let errorMessage = "알수없는 에러가 발생했습니다."
                    switch (error.response.status) {
                        case 400:  // Bad Request
                            errorMessage = error.response.data.message;
                            break;
                        default:
                            errorMessage = '알수없는 에러가 발생했습니다.';
                    }
                    passwordInvalidDiv.innerHTML = `<span style="color:red;">${errorMessage}</span>`;
                }
            })
    }

    // 이전 페이지 버튼 클릭
    const handlePrevPage = () => {
        if (page > 0) {
            setPage((prevPage) => prevPage - 1);
            handleSearch({ page: page - 1 });
        }
    };

    // 다음 페이지 버튼 클릭
    const handleNextPage = () => {
        setPage((prevPage) => prevPage + 1);
        handleSearch({ page: page + 1 });
    };


    const handleCreateClick = () => {
        const passwordInput = document.getElementById("createRoomPassword");
        const roomTitleInput = document.getElementById("createRoomTitle");
        const errorMessageDiv = document.getElementById('createRoomErrorMessage');

        const password = passwordInput.value.trim();
        const roomTitle = roomTitleInput.value.trim();

        // 에러 메시지 초기화 및 숨김
        errorMessageDiv.textContent = '';
        errorMessageDiv.style.display = 'none';


        axios
            .post(`${domain}/api/v1/rooms`, {
                title: roomTitle,
                password,
                participation: participant.members.map((member) => member.id)
            })
            .then((res) => {
                const roomId = res.data.data.roomId;
                closeCreateModal();
                navigate('/topic', {
                    state: {
                        roomId,
                        subUrl: `/sub/rooms/${roomId}/topics`,
                        userId,
                        leaderId: userId
                    }
                });
            })
            .catch((error) => {
                // 에러 처리
                if (error.response) {
                    switch (error.response.status) {
                        case 400:  // Bad Request
                            errorMessageDiv.textContent = error.response.data.message;
                            break;
                        default:
                            errorMessageDiv.textContent = '프로젝트 생성 중 오류가 발생했습니다.';
                    }
                    errorMessageDiv.style.display = 'block';
                } else {
                    console.error("Error creating project:", error);
                    errorMessageDiv.textContent = '알수없는 에러가 발생했습니다.';
                    errorMessageDiv.style.display = 'block';
                }
            });
    };

   const closeCreateModal = () => {
    setCreateModal(false);
    setNotJoinedRooms({num:0, rooms: []});
    setParticipant({num: 0, members: []});
  };

  const closeSearchModal = () => {
    setPage(0);
    setSearchExcludedRoomsTitle("")
    setSearchModal(false);
    setNotJoinedRooms({num:0, rooms: []});
  }

  const closeFriendModal = () => {
      setSearchFriendName("");
      setFriendModal(false);
  }

  const closeEnterModal = () => {
      setEnterRoomId(0);
      setEnterRoomTitle("");
      setEnterModal(false);
      setSearchModal(false);
  }

  const handleFriendList = () => {
    axios.get(`${domain}/api/v2/friends`)
        .then((res) => {
            const allResults = res.data.data.members; // 검색 결과
            const filteredResults = participant.num > 0
                ? allResults.filter((result) =>
                    !participant.members.some((member) => member.id === result.id)
                )
                : allResults;
            setAllResult({num: filteredResults.length, members: filteredResults})
            setViewResult({num: filteredResults.length, members: filteredResults})
            setFriendModal(true);
        })
        .catch(() => {
          console.log("Failed to list friend")
        })
  }
  /* 참여할 유저 ( 친구 상태 ) 검색 */
  const viewJoinFriends = (nickName) => {
      const searchRegExp = createFuzzyMatcher(nickName)
      const filteredAllResult = allResult.members?.filter((member) => {
          return searchRegExp.test(member.nickname)
      }) || [];

      setViewResult({
          num: filteredAllResult.length,
          members: filteredAllResult.filter(member =>
              // 이미 추가한 친구는 보이지 않는다.
              !participant.members.some(participantMember => participantMember.id === member.id)
          )
      });
  };


  const addResult = (addedFriend) => {
    setParticipant(prev => ({ num: prev.num + 1, members: [...prev.members, addedFriend ]})); // 참가자들 리스트 추가
    setViewResult((prev) => ({
        num: prev.num - 1,
        members: prev.members.filter((member) => member !== addedFriend)
    }))};

    const handleRemoveParticipant = (participationId) => {
        setParticipant((prev) => ({
            num: prev.num - 1, // num 값 감소
            members: prev.members.filter((member) => member.id !== participationId),
        }));
    };
    const ParticipantList = () => {

        return (
          <div className="participant_list">
            <h2>팀원 목록</h2>
            {participant.num > 0 ? (
                participant.members.map((participant) => (
                    <div key={participant.id} className="room_card">
                        <img src={participant.profile || userImage} alt="프로필"/>
                        <h2>{participant.nickname}</h2>
                        <button onClick={() => handleRemoveParticipant(participant.id)}>
                            X
                        </button>
                    </div>
                ))
            ) : <h3>선택한 팀원이 없습니다.</h3>}
          </div>
        );
    }
        return (
            <div className="container">
                <main>
                    <img src={mainlogo} className="under-logo"/>
                    <div className="search_box" onSubmit={(e) => e.preventDefault()}>
                        <input id="roomSearchInput"
                               className="project_search_txt"
                               type="text"
                               onChange={(e) => setSearchRoomsTitle(e.target.value)}
                               placeholder="참여중인 프로젝트를 검색하세요"/>
                        <button className="search_button" type="submit" onClick={() => setParticipationRoom(0)}>
                            <img src={searchIcon}/>
                        </button>
                    </div>
                    {/**/}

                    <div className="room_list">
                        <div className="header-container">
                            <h2 id="roomsListH">프로젝트 목록(참여한 프로젝트 : {rooms.num})
                                <div className="create_box" onSubmit={(e) => e.preventDefault()}>
                                    <div className="button-container">
                                        <button className="create_button" type="submit" onClick={() => setCreateModal(true)}>
                                            생성
                                        </button>
                                        <button className="create_button" type="click" onClick={() => setSearchModal(true)}>
                                            참가
                                        </button>
                                    </div>
                                </div>
                            </h2>
                        </div>
                        <div>
                            {rooms.num > 0 ? (
                                <>
                                    <div>
                                        {isLoading && (
                                            <div className="loading-overlay">
                                                <div className="spinner"></div>
                                                <p className="loading-text">Loading...</p>
                                            </div>
                                        )}
                                    </div>
                                    <div className="card-container">
                                        {rooms.rooms.map((room) => {
                                            const capColors = ["pink-cap", "green-cap", "orange-cap"];
                                            const randomCapClass = capColors[Math.round(Math.random() * capColors.length)];
                                            return (
                                                <div id={"room" + room.roomId} className="card">
                                                    <div className={`image-cap ${randomCapClass}`}>
                                                        {room.title}
                                                    </div>
                                                    <div className="card-body">
                                                        <span style={{fontSize: "small", justifyContent: "end", color: "white"}}>
                                                            참가자 : {room.participationNum}
                                                        </span>
                                                        <h3 className="card-title">주제 : {room.topic}</h3>
                                                        <div className="button-group">
                                                            <button className="card-button"
                                                                    onClick={() => enterRoom(room.roomId, room.title)}>
                                                                입장하기
                                                            </button>
                                                            <button className="card-red-button"
                                                                    onClick={() => deleteRoom(room.roomId)}>
                                                                삭제하기
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                    <div id="paginationButtonGroup" className="pagination-container">
                                        <button className="pagination-button" onClick={() => setParticipationRoom(rooms.firstPage - 1)}
                                                disabled={currentPage === rooms.firstPage - 1}>
                                            맨 처음
                                        </button>
                                        <div id="paginationButtonGroup" className="pagination-container">
                                            {/* 여기서 firstPage부터 lastPage까지 버튼 생성 */}
                                            {Array.from(
                                                {length: rooms.lastPage - rooms.firstPage + 1},
                                                (_, i) => rooms.firstPage + i
                                            ).map((page) => (
                                                <button className="pagination-button" onClick={() => setParticipationRoom(page - 1)}
                                                        disabled={currentPage === page - 1}>
                                                    {page}
                                                </button>
                                            ))}
                                        </div>
                                        <button onClick={() => setParticipationRoom(rooms.lastPage - 1)} className="pagination-button"
                                                disabled={currentPage === rooms.lastPage - 1}>
                                            마지막
                                        </button>
                                    </div>
                                </>
                            ) : <h1 style={{textAlign: "center", marginTop: "50px", padding: "20px"}} id="notExistH">
                                <div>
                                    <img src={emptyProject} height="300" width="300"/>
                                </div>
                                아직 참여하는 프로젝트가 없네요. 프로젝트에 참여해볼까요?
                            </h1>}
                        </div>
                        <div id="enterRoomModalDiv" className=""></div>
                    </div>

                    {/*<RoomList setCreateModal={setCreateModal}/>*/}
                </main>
                <Footer/>
                {searchModal && (
                            <div className="add_project_container">
                                <div className="modal_overlay" onClick={closeSearchModal}>
                                    <div className="search_modal_content" onClick={(e)=> e.stopPropagation()}>
                                        <h2 style={{textAlign : "center"}}>참여할 방 찾기</h2>
                                        <button className="close_button" onClick={() => closeSearchModal()}>
                                            X
                                        </button>
                                        <div className="search_modal_search_box">
                                            <input
                                                className="friend_search_txt"
                                                type="text"
                                                placeholder="참여할 방 제목을 입력하세요."
                                                value={searchExcludedRoomsTitle}
                                                onChange={e => setSearchExcludedRoomsTitle(e.target.value)}
                                            />
                                            <button
                                                className="search_button"
                                                type="submit"
                                                onClick={() => setNonParticipationRoom(0)}
                                            >
                                                <img src={searchIcon} alt="검색"/>
                                            </button>
                                        </div>
                                        <div className="scrollbar">
                                            {notJoinedRooms.num > 0 ? (
                                                <>
                                                    <div className="card-container">
                                                        {notJoinedRooms.rooms.map((room) => {
                                                            // 랜덤 배경색 클래스 설정
                                                            const capColors = ["pink-cap", "green-cap", "orange-cap"];
                                                            const randomCapClass = capColors[Math.round(Math.random() * capColors.length)];

                                                            return (
                                                                <div key={room.roomId} className="card">
                                                                    {/* 동적으로 랜덤 클래스 추가 */}
                                                                    <div
                                                                        className={`image-cap ${randomCapClass}`}>{room.title}</div>
                                                                    <div className="card-body">
                                                                        <span style={{fontSize: "small", justifyContent: "end", color: "white"}}>
                                                                            참가자 : {room.participationNum}
                                                                        </span>
                                                                        <h3 className="card-title">{room.topic}</h3>
                                                                        <button className="card-button"
                                                                                onClick={() => enterRoom(room.roomId, room.title)}>
                                                                            참여
                                                                        </button>
                                                                    </div>
                                                                </div>
                                                            );
                                                        })}
                                                    </div>
                                                    <div id="paginationButtonGroup" className="pagination-container">
                                                        <button className="pagination-button" onClick={() => setNonParticipationRoom(notJoinedRooms.firstPage - 1)}
                                                                disabled={notJoinedRoomPage === notJoinedRooms.firstPage - 1}>
                                                            맨 처음
                                                        </button>
                                                        <div id="paginationButtonGroup" className="pagination-container">
                                                            {/* 여기서 firstPage부터 lastPage까지 버튼 생성 */}
                                                            {Array.from(
                                                                {length: notJoinedRooms.lastPage - notJoinedRooms.firstPage + 1},
                                                                (_, i) => notJoinedRooms.firstPage + i
                                                            ).map((page) => (
                                                                <button className="pagination-button" onClick={() => setNonParticipationRoom(page - 1)}
                                                                        disabled={notJoinedRoomPage === page - 1}>
                                                                    {page}
                                                                </button>
                                                            ))}
                                                        </div>
                                                        <button onClick={() => setNonParticipationRoom(notJoinedRooms.lastPage - 1)} className="pagination-button"
                                                                disabled={notJoinedRoomPage === notJoinedRooms.lastPage - 1}>
                                                            마지막
                                                        </button>
                                                    </div>
                                                </>
                                            ) : <h2 style={{textAlign: "center"}} id="notExistProjectH">
                                                검색한 프로젝트가 존재하지 않습니다.</h2>}
                                        </div>
                                    </div>
                                </div>
                            </div>
                )}

                {createmodal && (
                    <div className="modal_overlay" onClick={() => setCreateModal(false)}>
                        <div onClick={(e) => e.stopPropagation()} className="create_modal_content">
                            <button className="close_button" onClick={() => closeCreateModal()}>
                                X
                            </button>
                            <div
                                id="createRoomErrorMessage"
                                className="error-message"
                                style={{
                                    color: 'red',
                                    textAlign: 'center',
                                    marginTop: '10px',
                                    display: 'none'
                                }}
                            ></div>
                            <div className="modal_body">
                                <div className="modal_section">
                                    <label className="modal_label">방 제목</label>
                                    <input className="modal_input" id="createRoomTitle" type="text"/>
                                </div>

                                <div className="modal_section">
                                    <label className="modal_label">비밀번호</label>
                                    <input style={{fontFamily: 'Arial, sans-serif'}} className="modal_input" id="createRoomPassword" type="password"
                                    />
                                </div>
                            </div>
                            <div className="add_friend">
                                <ParticipantList/> {/* 참가할 친구 리스트 */}
                                <button className="common-button" onClick={() => handleFriendList()}>
                                    +
                                </button>
                            </div>
                            <button className="common-button" onClick={() => handleCreateClick()}>생성</button>
                        </div>
                    </div>
                )}

                {enterModal && (
                    <div className="enter_modal_overlay" onClick={closeEnterModal}>
                        <div className="enter_modal_content" style={{textAlign: "center"}} onClick={(e)=> e.stopPropagation()}>
                            <button className="close_button" onClick={() => closeEnterModal()}>
                                X
                            </button>
                            <label className="enter_modal_label">
                                제목 : {enterRoomTitle}
                            </label>
                            <div id="passwordInvalidDiv" style={{color : "gray"}}>비밀번호를 입력해주세요.</div>
                            <input style={{fontFamily: 'Arial, sans-serif'}} className="enter_modal_input" id="roomPasswordInput" type="password"/>
                            <button className="enter_button" onClick={() => verifyPasswordAndEnterRoom(enterRoomId)}> 입장 </button>
                        </div>
                    </div>
                )}

                {friendModal && (
                    <div className="friend_overlay" onClick={()=> closeFriendModal()}>
                        <div onClick={(e)=> e.stopPropagation()} className="friend_modal">
                        <input
                            className="participant_search_txt"
                            type="text"
                            placeholder="친구 이름을 입력하세요."
                            onChange={(e) => setSearchFriendName(e.target.value)}
                            value={searchFriendName}
                        />
                            <button className="close_button" onClick={() => closeFriendModal()}>
                                X
                            </button>
                        <div className="search_friend_list scrollbar">
                            {viewResult.num > 0 ? (
                                viewResult.members.map((member) => (
                                    <div key={member.email} className="friend_card">
                                        <img src={member.profile || userImage} alt="프로필"/>
                                        <h2>{member.nickname}</h2>
                                        <button className="add_result_button"
                                            onClick={() => addResult(member)}> 초대
                                        </button>
                                    </div>
                                ))
                            ) : <h2>친구가 없습니다.</h2>}
                        </div>
                        </div>
                    </div>
                )}
    </div>
  );
};

export default Project;
