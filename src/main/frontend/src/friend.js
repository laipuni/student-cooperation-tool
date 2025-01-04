import React, { useEffect, useState } from "react";
import axios from "axios";
import searchIcon from "./images/search.svg";
import personHeart from "./images/PersonHearts.svg"
import friendship from "./images/friendship.svg"
import {domain} from "./domain";
import "./friend.css";
import "./scrollbar.css"
import "./bar.css"
import "./juaFont.css"
import Footer from "./footer";
import mainlogo from "./images/mainlogo.png";


const Friend = () => {
    const [allFriends, setAllFriends] = useState({num : 0, friends : []}) // 전체 친구 목록
    const [newFriends, setNewFriends] = useState({num : 0, friends : []})
    const [searchFriendName, setSearchFriendName] = useState("");
    const [searchUserName, setSearchUserName] = useState("");
    const [modalOpen, setModalOpen] = useState(false);
    const [searchUsers, setSearchUser] = useState({num:0, users:[]})
    const [friends, setFriends] = useState({num: 0, members: []});
    const [debouncedUserSearch, setDebouncedUserSearch] = useState("");
    const [debouncedFriendSearch, setDebouncedFriendSearch] = useState(""); // 디바운스된 검색값


    function getFriends() {
        axios
            .get(domain + "/api/v1/friends")
            .then((res) => {
                setFriends(res.data.data);
                setAllFriends(res.data.data || {num : 0, friends: []});
            })
            .catch(() => {
                console.log("failed to load friends");
            });
    }

    useEffect(() => {
        getFriends();
    },[]);


    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedFriendSearch(searchFriendName);
        }, 500); // 1초 대기

        return () => clearTimeout(handler); // 이전 타이머를 정리
    }, [searchFriendName]);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedUserSearch(searchUserName);
        }, 500); // 1초 대기

        return () => clearTimeout(handler); // 이전 타이머를 정리
    }, [searchUserName]);

    useEffect(() => {
        if (debouncedFriendSearch) {
            findFriend(debouncedFriendSearch); // 검색 실행
        } else {
            setFriends(allFriends) // 검색어가 없을 경우 원래 전체 친구 목록으로 보이기
        }
    }, [debouncedFriendSearch]);

    useEffect(() => {
        if (debouncedUserSearch) {
            findUser(debouncedUserSearch); // 검색 실행
        }
    }, [debouncedUserSearch]);

    const FriendsList = () => {
        return (
            <div className="friend_list">
                <h2 id="friendsListH"></h2>
                {friends.num > 0 ? (
                    <ul>
                        <div className="friends-li">
                            <h2>친구 목록</h2>
                            <div className="friends-card">
                                {friends.members.map(friend => (
                                    <li key={friend.email}>
                                        <div className="profile-icon">
                                            <img src={friend.profile} alt="프로필"/>
                                        </div>
                                        <span className="friend-name">{friend.nickname}</span>
                                    </li>)
                                )}
                            </div>
                        </div>
                    </ul>
                ) : <></>}

                {newFriends.num > 0 || friends.num > 0 ?
                    <></> :
                    (<h1 style={{textAlign : "center", width: "1000px"}} id="notExistH">
                        <div>
                            <img src={friendship} height="300" width="300" style={{marginTop: "20px"}}/>
                        </div>
                        아직 등록된 친구가 없네요. 친구들을 찾아 볼까요?
                    </h1>)
                }
            </div>
        );
    };

    const getSearchResult = (nickName,relation) => {
        if (!nickName || nickName.trim() === "") {
            // 검색어가 없을 경우 검색하지 않음
            return Promise.resolve(undefined);
        }

        return axios
            .get(`${domain}/api/v1/friends/search?relation=${relation}&name=${nickName}`)
            .then((res) => {
                console.log(res.data.data);
                return res.data.data;
            })
            .catch((error) => {
                console.error(error);
                return undefined;
            });
    };

    const handleAddFriend = (id, profile, nickname) => {
        axios
            .post(`${domain}/api/v1/friends`, {
                friendId: id
            },  { "Content-Type": "application/json"},)
            .then((res) => {
                putNewFriend(id,profile, nickname);
                setSearchUser((prevUsers) => ({
                    ...prevUsers,
                    num: prevUsers.num - 1,
                    users: prevUsers.users.filter(friend => friend.id !== id),
                }));
            })
            .catch(() => {
                console.log("Failed to add friend.");
            });
    };

    function putNewFriend(id, profile, nickname) {
        const newFriend = {
            id: id,
            profile: profile,
            nickname: nickname,
        };

        setNewFriends((preFriends) => ({
            ...preFriends,
            num: preFriends.num + 1, // num 값을 증가시킴
            friends: [...preFriends.friends, newFriend], // 기존 배열에 새 친구 추가
        }));
    }

    const handleCloseModal = () => {
        setModalOpen(false);
        setSearchUser({num : 0, users: []}); // 모달창 닫을 때 데이터 초기화
        setSearchUserName("")
    };

    const findUser = (nickName) => {
        getSearchResult(nickName, false)
            .then((results) => {
                console.log("Search Results:", results);
                setSearchUser((prevUser) => ({
                    ...prevUser,
                    num: results.num,
                    users: results.members,
                }));
            })
            .catch((error) => {
                console.error("Error fetching results:", error);
            });
    }

    const findFriend = (nickName) => {
        const filteredFriends = allFriends.friends?.filter((friend) =>
            friend.nickname?.includes(nickName)
        ) || [];
        console.log(allFriends);
        console.log(filteredFriends);
        setFriends({
            num : filteredFriends.length,
            friends: filteredFriends
        });
    }

    return (
        <div className="container">
            <div className="friend-main">
                <img src={mainlogo} className="under-logo"/>
                <div className="search_box">
                    <input
                        className="friend_search_txt"
                        type="text"
                        placeholder="친구 이름을 입력하세요."
                        value={searchFriendName}
                        onChange={(e) => setSearchFriendName(e.target.value)}
                    />
                    <button
                        className="search_button"
                        type="submit"
                        onClick={() => findFriend(searchFriendName,true)}
                    >
                        <img src={searchIcon} alt="검색"/>
                    </button>
                </div>
                <div>
                    <button className="add_friend_modal_button" onClick={() => setModalOpen(true)}>
                        친구 추가
                    </button>
                </div>
                <div className="friends-list-wrapper">
                    {newFriends.num > 0 ? (
                        <>
                            <div className="friend_list">
                                <div className="friends-li">
                                    <h2>추가된 친구</h2>
                                    <ul>
                                        <div className="friends-card">
                                            {newFriends.friends.map(newFriend => (
                                                <li key={newFriend.id}>
                                                    <div className="profile-icon">
                                                        <img src={newFriend.profile} alt={`${newFriend.nickname} 프로필`}/>
                                                    </div>
                                                    <span className="friend-name">{newFriend.nickname}</span>
                                                </li>)
                                            )}
                                        </div>
                                    </ul>
                                </div>
                            </div>
                        </>
                    ) : <></>
                    }
                </div>
                <div className="friends-list-wrapper">
                    <FriendsList/> {/* 친구 목록 표시 */}
                </div>
            </div>
            <Footer/>

            {modalOpen && (
                <div className="modal_overlay" onClick={handleCloseModal}>
                    <div className="friend_modal_content" onClick={(e) => e.stopPropagation()} >
                        <h3>친구 추가</h3>
                        <button className="close_button" onClick={handleCloseModal}>
                            X
                        </button>
                        <div className="add_friend_modal_search_box">
                            <input
                                className="friend_search_txt"
                                type="text"
                                placeholder="검색할 유저 이름을 입력하세요."
                                value={searchUserName}
                                onChange={(e) => setSearchUserName(e.target.value)}
                            />
                            <button
                                className="search_button"
                                type="submit"
                                onClick={() => findUser(searchUserName,false)}
                            >
                                <img src={searchIcon} alt="검색"/>
                            </button>
                        </div>
                        <div className="friend_result scrollbar">
                            <ul>
                                {searchUsers.num !== 0 ? (
                                    searchUsers.users.map(user => (
                                            <li key={user.email}>
                                                <div className = "friend_profile">
                                                    <div className="profile-icon">
                                                        <img src={user.profile} alt="프로필"/>
                                                    </div>
                                                    <span className="friend-name">{user.nickname}</span>
                                                    <button className="add_friend_button" onClick={() => handleAddFriend(user.id,user.profile,user.nickname)}>
                                                        +
                                                    </button>
                                                </div>
                                            </li>)
                                    ))
                                 : <></>}
                            </ul>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Friend;