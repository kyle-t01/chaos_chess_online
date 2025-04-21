// store important global variables

import { useContext, createContext, useState, useRef } from "react";

const GlobalContext = createContext();

export const GlobalContextProvider = ({ children }) => {

    const socketRef = useRef(null);
    const [playerName, setPlayerName] = useState("");
    const [hasConnected, setHasConnected] = useState(false);
    const [hasJoined, setHasJoined] = useState(false);
    const [lobby, setLobby] = useState([]);
    const [hasGameStarted, setHasGameStarted] = useState(false);

    // move, moved, 

    const [timeLeft, setTimeLeft] = useState(0);
    const [totalTime, setTotalTime] = useState(0);


    // connect to websocket automatically
    useEffect(() => {
        if (playerName && !hasConnected) {
            console.log("connecting to websocket...")
            handlePlayerConnect();
        }

        // Optional cleanup: disconnect on unmount
        return () => {
            if (socketRef.current) {
                console.log("closing websocket...")
                socketRef.current.close();
            }
        };
    }, []); // Only run once on mount


    // ie: sendEvent(STRING, Object)
    const sendEvent = (type, data) => {
        const event = {
            type: type.toUpperCase(),
            data: data,
        };
        socketRef.current.send(JSON.stringify(event));
        console.log(`sent an event: ${type}: ${data}`);
    }
    const handleStartGame = () => {
        sendEvent("START", "");
    }


    // when the player connects to the lobby, open connection to websocket
    const handlePlayerConnect = () => {
        const userName = playerName.trim() ?? ""
        if (!playerName.trim()) return;
        // close existing socket
        if (socketRef.current) {
            socketRef.current.close();
        }
        // attempt connection
        if (!socketRef.current || socketRef.current.readyState === WebSocket.CLOSED) {
            socketRef.current = new WebSocket('ws://localhost:8080/game');
        }
        // establish connection
        socketRef.current.onopen = () => {
            console.log('websocket open!');
            sendEvent('join', userName);
        };
        // receive message
        socketRef.current.onmessage = (message) => {
            handleEventMessage(message);
        };
        // error
        socketRef.current.onerror = (e) => {
            console.log('websocket error!', e);
        };
        // disconnect
        socketRef.current.onclose = () => {
            console.log('websocket disconnected!');
        };
    };

    // messages sent from the game server
    const handleEventMessage = (message) => {
        const eventJSON = JSON.parse(message.data);
        console.log("server sent data:");
        console.log(eventJSON);
        switch (gameEvent.type) {
            case "TIME":
                const time = eventJSON.data
                setTimeLeft(time);
                break;
            case "TOTAL_TIME":
                setTotalTime(eventJSON.data);
                break;
            case "JOIN":
                setHasJoined(true);
                console.log("You have joined the lobby!");
                break;
            case "LOBBY_UPDATE":
                setLobby(eventJSON.data);
                console.log("Updating current lobby!");
                break;
            case "START":
                setHasGameStarted(true);
                console.log("Starting or Joining an existing game!");
                break;
            case "END":
                setHasGameStarted(false);
                setUserAnswer(null);
                setIsShowAnswer(false)
                setQuestion(null)
                console.log("Game has ended!");
                break;
            case "QUESTION":
                setIsShowAnswer(false);
                setQuestion(eventJSON.data);
                setUserAnswer(null);
                console.log("GOT A QUESTION");
                break;
            case "SHOW":
                setIsShowAnswer(true);
                console.log("SHOWING CURRENT ANSWERS");
                break;
            case "KICK":
                setHasGameStarted(false);
                setHasJoined(false)
                // check if player has been KICKED
                console.log("You were KICKED from the game!");
                alert("A game is already in progress, wait for it to finish before joining!");
                socketRef.current.close();
                break;
            case "ANSWER":
                console.log("Your answer was received!");
                break;
            default:
                alert("UNKNOWN IMPLEMENTATION of", eventJSON);
        }
    }


    return (
        <GlobalContext.Provider
            value={{
                socketRef,
                playerName, setPlayerName,
                hasJoined, setHasJoined,
                hasConnected, setHasConnected,
                lobby, setLobby,
                hasGameStarted, setHasGameStarted,
                question, setQuestion,
                userAnswer, setUserAnswer,
                isShowAnswer, setIsShowAnswer,
                timeLeft, totalTime,
                sendGameEvent,
                handleStartGame,
                handlePlayerJoin,
                handleUserAnswer,
            }}>
            {children}
        </GlobalContext.Provider>
    );
}

// export the context 
export const GlobalVars = () => {
    return useContext(GlobalContext)
}