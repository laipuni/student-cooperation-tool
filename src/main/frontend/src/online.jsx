import React, { useState } from 'react';
import "./online.css"

const Online = ({online}) => {
    const [showList, setShowList] = useState(false);

    const toggleList = () => {
        setShowList(!showList);
    };

    return (
        <div className="online-body">
            <div className="online-button-container">
                <button className="online-button" onClick={toggleList}>
                    온라인 : {online.num}명
                </button>
            </div>
            {showList && (
                <div className="user-list">
                    {online.num > 0 ?
                        (online.online.map((user) => (
                            <div key={user.memberId} className="user-item">
                                <img src={user.profile} className="profile"></img>
                                <div className="name">{user.nickName} 🟢</div>
                            </div>
                        ))) : <span>온라인 상태인 유저가 없습니다.</span>
                    }
                </div>
            )}
        </div>
    );
};

export default Online;