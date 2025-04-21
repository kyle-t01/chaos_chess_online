// store important global variables

import { useContext, createContext, useState, useRef, useEffect } from "react";

const GlobalContext = createContext();

export const GlobalContextProvider = ({ children }) => {

    const socketRef = useRef(null);
    const [playerName, setPlayerName] = useState("");
    const [hasConnected, setHasConnected] = useState(false);
    const [hasJoined, setHasJoined] = useState(false);
    const [lobby, setLobby] = useState([]);
    const [hasGameStarted, setHasGameStarted] = useState(false);
    const [gameState, setGameState] = useState(null);

    // move, moved, 

    const [timeLeft, setTimeLeft] = useState(0);
    const [totalTime, setTotalTime] = useState(0);


    // connect to websocket automatically
    useEffect(() => {
        handlePlayerConnect()

    }, []);


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

    const handlePlayerJoinNorth = () => {
        sendEvent("JOIN", "N");
    }

    const handlePlayerJoinSouth = () => {
        sendEvent("JOIN", "S");
    }

    // when the player connects to the lobby, open connection to websocket
    const handlePlayerConnect = () => {
        const userName = playerName.trim() ?? ""

        // attempt connection
        if (!socketRef.current || socketRef.current.readyState === WebSocket.CLOSED) {
            socketRef.current = new WebSocket('ws://localhost:8080/game');
        }
        // establish connection
        socketRef.current.onopen = () => {
            console.log('websocket open!');
            sendEvent('CONNECT', userName);
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
        switch (eventJSON.type) {
            case "TIME":
                const time = eventJSON.data
                setTimeLeft(time);
                break;
            case "TOTAL_TIME":
                setTotalTime(eventJSON.data);
                break;
            case "CONNECTED":
                setHasConnected(true);
                console.log("You have connected to the main lobby!");
                break;
            case "UPDATE_CONNECTED":
                setLobby(eventJSON.data);
                console.log("Updating current lobby!");
                break;
            case "JOINED":
                setGameState(eventJSON.data)
                console.log("You joined, the game state is, ", eventJSON.data);
                break;
            case "GAME_STATE_UPDATED":
                setGameState(eventJSON.data)
                console.log("the game state is:", eventJSON.data);
                break;
            case "START":
                setHasGameStarted(true);
                console.log("Starting or Joining an existing game!");
                break;
            case "END":
                setHasGameStarted(false);
                console.log("Game has ended!");
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
                console.log("UNKNOWN IMPLEMENTATION of", eventJSON.type);
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
                timeLeft, totalTime,
                sendEvent,
                handleStartGame,
                handlePlayerConnect,
                handlePlayerJoinNorth, handlePlayerJoinSouth,
            }}>
            {children}
        </GlobalContext.Provider>
    );
}

// export the context 
export const GlobalVars = () => {
    return useContext(GlobalContext)
}