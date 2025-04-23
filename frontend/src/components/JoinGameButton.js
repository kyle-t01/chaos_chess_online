import { GlobalVars } from "../context/GlobalContext";


const JoinGameButton = () => {

    // global state
    const { handlePlayerJoinNorth, handlePlayerJoinSouth, hasJoined, gameState } = GlobalVars();

    const renderJoinSouthButton = () => {
        return (
            <button className="button" onClick={handlePlayerJoinSouth} hidden={gameState?.southPlayer ?? false} >
                Join Game as Team South
            </button>
        );
    }

    const renderJoinNorthButton = () => {
        return (
            <button className="button" onClick={handlePlayerJoinNorth} hidden={gameState?.northPlayer ?? false}>
                Join Game as Team North
            </button>
        );
    }
    return (
        <div className="join-game-buttons">
            {renderJoinSouthButton()}
            {renderJoinNorthButton()}

        </div>
    );

}

export default JoinGameButton;