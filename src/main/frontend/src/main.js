import React, { useEffect, useState } from "react";
import "./main.css";
import Footer from "./footer";


const Main = () => {
    return (
        <div className="container">
            <main className="main-page">
                <h1 className="main-title">STOOL에 오신 것을 환영합니다!</h1>
                <p className="main-description">
                    저희는 보다 간편한 협업툴을 지향합니다.<br />
                    학생 분들을 보다 간편하고 즐거운 협업을 도와주기 위함이 저희 목적입니다.
                </p>
                <button className="gradient-button">Let's Go!</button>
            </main>
           <Footer/>
        </div>
    );
};

export default Main;
