import { GlobalVars } from "../context/GlobalContext";


const JoinGameButton = () => {

    // global state
    const { handlePlayerJoinNorth, handlePlayerJoinSouth, hasJoined, gameState } = GlobalVars();


    return (
        <div className="join-game-buttons" hidden={hasJoined}>
            <button className="button" onClick={handlePlayerJoinSouth} hidden={gameState?.southPlayer}>
                Join Game (as Team South)
            </button>
            <button className="button" onClick={handlePlayerJoinNorth} hidden={gameState?.northPlayer}>
                Join Game (as Team North)
            </button>
        </div>
    );

}

export default JoinGameButton;