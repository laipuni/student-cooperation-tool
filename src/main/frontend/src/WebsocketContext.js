import React, { createContext, useContext, useRef, useState, useEffect } from 'react';
import {useLocation} from 'react-router-dom';// 방 id를 받아오기 위해 선언한 import
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { domain } from './domain';

const WebSocketContext = createContext();

export const useWebSocket = () => {
    return useContext(WebSocketContext);
};

export const WebSocketProvider = ({ children }) => {
    const [isConnected, setIsConnected] = useState(false);
    const stompClient = useRef(null);
    const location = useLocation();
    const { roomId, subUrl, userId, leaderId, presentationId } = location.state || {};
    const websocketId = useRef(null);

    // 온라인 상태 state
    const [online, setOnline] = useState({num : 0, online : []})
    const onlineSubscribe = useRef([]); // 온라인 상태 채널 구독 객체
    const errorSubscribe = useRef([]); // 에러 메세지 채널 구독 객체


    const updateOnline = (online) => {
        //온라인 유저 추가
        setOnline((prevOnline) => ({
            ...prevOnline,
            num: online.onlineNum, // 주제 개수 증가
            online: online.online
        }));
    }

    const offOnline = () => {
        // 유저가 방을 나갈 경우 Online을 구독을 끊고, offline는 메세지를 다른 유저에게 보냄
        const data = {
            roomId : roomId
        }

        stompClient.current.publish({
            destination : `/pub/room/exit`,
            body : JSON.stringify(data)
        })

        onlineSubscribe.current.unsubscribe(); //온라인 상태 구독 취소
    }


    const receiveMessage = (message) => {
        //3-1 구독한 url에서 온 메세지를 받았을 때
        const frame = JSON.parse(message.body)

        if(frame.messageType === "ROOM_ENTER" || frame.messageType === "ROOM_EXIT"){
            //온라인 유저 등록
            updateOnline(frame.data)
        } else {
            console.log("Not Supported Message Type")
        }
    }

    const receiveError = (error) => {
        //3-2 구독한 url에서 온 메세지를 못 받아 에러가 발생했을 때
        alert("방 입장에 실패하였습니다.");
        console.error("STOMP Error", error);
        window.location.href = "/";
    }

    const onStompError = (error) => {
        //2-2 연결 실패의 경우
        alert("방 입장에 실패하였습니다.");
        console.error("STOMP Error", error);
        window.location.href = "/";
    }

    const setOnlineSub = () => {
        onlineSubscribe.current = stompClient.current.subscribe(
            `/sub/rooms/${roomId}/online`,
            receiveMessage,
            receiveError
        )
        const data = {
            roomId : roomId
        }

        stompClient.current.publish({
            destination : `/pub/room/enter`,
            body : JSON.stringify(data)
        })
    }

    const setErrorChannelSub = () =>{
        errorSubscribe.current = stompClient.current.subscribe(
            `/user/${websocketId.current}/sub/errors`,
            (message) => {
                const frame = JSON.parse(message.body);
                alert(frame.message)
            },
            (error) =>{
                console.log(error)
            }
        )
    }

    const onConnect = (frame) => {
        // 에러 메세지 채널을 위한 유저의 개인 websocketId를 저장
        websocketId.current = frame.headers['user-name']
        setIsConnected(true)
        setOnlineSub()
        setErrorChannelSub()
    }

    useEffect(() => {
        stompClient.current = new Client({
            webSocketFactory: () => new SockJS(`${domain}/ws-stomp`),
            connectHeaders: {
                SubscribeUrl : subUrl // 어디에 구독할 지 헤더에 담아서 보냄
            },
            reconnectDelay: 5000,
            onConnect,
            onStompError,
        });

        stompClient.current.activate();

        return () => {
            if (stompClient.current) {
                offOnline();
                errorSubscribe.current.unsubscribe();
                stompClient.current.deactivate();
            }
        };
    }, []);

    return (
        <WebSocketContext.Provider value={{ stompClient, isConnected, roomId, userId, leaderId, presentationId, online}}>
            {children}
        </WebSocketContext.Provider>
    );
};