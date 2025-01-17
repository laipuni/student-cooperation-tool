import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { domain } from "./domain";
import { useWebSocket } from './WebsocketContext'; // WebSocketProvider의 훅 사용
import "./chatroom.css";

function ChatRoom() {
    const [chatList, setChatList] = useState([]);
    const [inputMessage, setInputMessage] = useState("");
    const chatRef = useRef(null);
    const fetchedRef = useRef(false);
    const [isShowDownBtn, setIsShowDownBtn] = useState(false);
    const [lastMessageId, setLastMessageId] = useState(null);
    const [newMessage, setNewMessage] = useState(null); // 새 메시지 정보
    const [scrollDown, setScrollDown] = useState(false);
    const [isTop, setIsTop] = useState(false);
    const {stompClient, isConnected, roomId, userId} = useWebSocket(); // WebSocket 연결 관리
    const prevScrollHeight = useRef(null);
    const subscriptions = useRef([]); // 구독후 반환하는 객체로, 해당 객체로 구독을 취소해야 한다.

    //==================================채팅방 구현 내용========================================
    const chatFetch = async () => {
        try {
            const res = await axios.get(`${domain}/api/v1/rooms/${roomId}/chats`, {
                params: {
                    page: 0,
                    lastMessageId,
                },
            });
            const newData = res.data.data.chats.reverse();
            if (newData.length > 0) {
                fetchedRef.current = true;
                setLastMessageId(newData[0].chatId);
                setChatList((prev) => [...newData, ...prev]);
                prevScrollHeight.current=chatRef.current.scrollHeight;
            }
            else{
                fetchedRef.current = false;
            }
        } catch (error) {
            console.error("채팅 내역을 불러오는데 실패했습니다.", error);
        }
    };

    const scrollDownButton = () => {
        if (chatRef.current) {
            chatRef.current.scrollTop = chatRef.current.scrollHeight;
            if(newMessage!=null) setNewMessage(null);
        }
    }
    const onScroll = () => {
        const messageRef = chatRef.current;
        const scrollTop = messageRef.scrollTop;
        const scrollBottom = messageRef.scrollHeight - messageRef.scrollTop - messageRef.clientHeight
        if (scrollTop === 0) {
            setIsTop(true);}
        if (scrollBottom <= 0 && newMessage !== null) setNewMessage(null);

        //스크롤이 일정 길이만큼 올라갔을 때 내려가는 버튼 표시
        if (messageRef.scrollHeight - scrollTop <= messageRef.clientHeight + 50) {  // 50은 여유 공간
            setIsShowDownBtn(false);
        } else {
            setIsShowDownBtn(true);
        }
    };

    const handleNewMessageClick = () => {
        setScrollDown((prev) => !prev) // 알림 클릭 시 스크롤을 맨 아래로 이동
        setNewMessage(null);
    };

    //초기화면 세팅(유저정보, 채팅 내역, 스크롤바 최하단으로)
    useEffect(() => {
        chatFetch().then(() => {
            if(chatList) {
                setScrollDown((prev) => !prev);
            }
        });
    }, []);

    //스크롤 최하단으로 내림 (랜더링)
    useEffect(() => {
        if (chatRef.current) {
            chatRef.current.scrollTop = chatRef.current.scrollHeight;
        }
    }, [scrollDown]);

    // isTop이 바뀔때마다 작동하고 isTop이 ture일때만 스크롤 제 위치로 이동
    useEffect(() => {
        if (isTop) {
            chatFetch()
            }
        setIsTop(false);
    }, [isTop]);

    // fetch를 통해 채팅이 추가됐을 때 스크롤 원위치로
    useEffect(() => {
            if(fetchedRef.current && prevScrollHeight.current) {
                chatRef.current.scrollTop = chatRef.current.scrollHeight - prevScrollHeight.current;
                fetchedRef.current = false;
            }
    }, [chatList]);

    //===============================================================================

    //===================================소켓 연결=====================================
    useEffect(() => {
        if (isConnected) {
            onConnect(); // 연결이 완료되면 onConnect 호출
        }
        return () => {
            if (stompClient.current) {
                subscriptions.current.unsubscribe();
            }
        };
    }, [isConnected]); //isConnected 상태가 바뀌면 실행된다.

    const receiveError = (error) => {
        //3-2 구독한 url에서 온 메세지를 못 받아 에러가 발생했을 때
        alert("채팅방 입장에 실패하였습니다.");
        console.error("STOMP Error", error);
        window.location.href = "/";
    }

    const receiveMessage = (message) => {
        //3-1 구독한 url에서 온 메세지를 받았을 때
        const frame = JSON.parse(message.body)

        if(frame.messageType === "CHAT_ADD"){
            addChatInChatRoom(frame.data)
        }
        else if(frame.messageType === "CHAT_DELETE"){
            deleteChatInChatRoom(frame.data)
        }
        else {
            console.log("Not Supported Message Type")
        }
    }

    const onConnect = () => {
        //2-1 연결 성공의 경우
        subscriptions.current = stompClient.current.subscribe(
            `/sub/rooms/${roomId}/chat`, receiveMessage, receiveError);
        console.log('connected chat');
    }
    //================================================================================

    //=================================소켓 기능 구현(추가, 삭제)=========================
    // 채팅 등록
    const addChatInChatRoom = (frame) => {
        const newMessage = {
            chatId: frame.chatId,
            nickName: frame.nickName,
            content: frame.content,
            userId: frame.memberId,
        };
        setChatList((prev) => [...prev, newMessage]);
        if (newMessage.userId === userId) {
            setScrollDown((prev) => !prev);
        } else {
            // 본인이 아니면, 스크롤이 맨 아래에 있지 않으면 알림을 보여줌
            const messageRef = chatRef.current;
            if (messageRef && messageRef.scrollTop + messageRef.clientHeight < messageRef.scrollHeight) {
                setNewMessage(newMessage); // 새 메시지 알림 표시
            }
            else{
                setScrollDown((prev) => !prev);
            }
        }
    }

    const handleSendMessage = () => {
        const data = {
            roomId : roomId,
            content : inputMessage
        }
        stompClient.current.publish({
            destination: '/pub/chats/add',
            body: JSON.stringify(data)
        })
        setInputMessage("");
    }

    const handleKeyPress = (e) => {
        if (e.key === "Enter") {
            handleSendMessage();
        }
    };

    //채팅 삭제
    const deleteChatInChatRoom = (frame) => {
        const chatId = frame.chatId;
        setChatList((prev) =>
            prev.filter((t) => t.chatId !== chatId));
    }

    const handleDeleteMessage = (chatId) => {
        const data = {
            roomId : roomId,
            chatId : chatId
        }
        stompClient.current.publish({
            destination: '/pub/chats/delete',
            body: JSON.stringify(data)
        })
    }
    //================================================================================

    return (
        <div className="chat-container">
            <div className="chat-header">채팅방</div>
            <div
                ref={chatRef}
                onScroll={onScroll}
                className="chat-messages"
            >
                {chatList.length > 0 ? (
                    chatList.map((chat) => (
                        <ChatMessage
                            key={chat.chatId}
                            message={chat}
                            isMine={userId === chat.userId}
                            handleDeleteMessage={handleDeleteMessage}
                        />
                    ))
                ) : (
                    <div style={{ textAlign: "center", color: "#999" }}>채팅이 없습니다.</div>
                )}

                {newMessage && newMessage.userId !== userId && (
                    <div
                        className="new-message-notification"
                        onClick={handleNewMessageClick}
                    >
                        <div className="new-message-head">
                            {newMessage.nickName.charAt(0)} {/* 발신자의 첫 글자를 표시 */}
                        </div>
                        <span className="new-message-content">{newMessage.nickName} {newMessage.content}</span>
                    </div>
                )}

                {isShowDownBtn && (
                    <button
                        onClick={scrollDownButton}
                        className="scroll-down-button"
                    >
                        ˅
                    </button>
                )}
            </div>
            <div className="chat-input-container">
                <input
                    type="text"
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyPress={handleKeyPress}
                    className="chat-input"
                    placeholder="메시지를 입력하세요"
                />
                <button onClick={handleSendMessage} className="chat-send-button">
                    <span className="arrow">↑</span>
                </button>
            </div>
        </div>
    );
}

//================================채팅별 출력양식======================================
function ChatMessage({ message, isMine, handleDeleteMessage }) {
    return (
        <div
            className={`chat-message-container ${isMine ? "chat-message-mine" : "chat-message-other"}`}
        >
            {!isMine && <div className="chat-author">{message.nickName}</div>}
            <div className="chat-message">{message.content}</div>
                {isMine && (
                    <button className="delete-button" onClick={() => handleDeleteMessage(message.chatId)}>
                        X
                    </button>
                )}
        </div>
    );
}

export default ChatRoom;
