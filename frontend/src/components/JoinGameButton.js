import { GlobalVars } from "../context/GlobalContext";


const JoinGameButton = () => {

    // global state
    const { handlePlayerJoinNorth, handlePlayerJoinSouth, hasJoined } = GlobalVars();


    return (
        <div className="input-box" hidden={hasJoined}>
            <h2>Join Current Game here..</h2>
            <button className="button" onClick={handlePlayerJoinSouth}>
                Join Game (as Team South)
            </button>
            <button className="button" onClick={handlePlayerJoinNorth}>
                Join Game (as Team North)
            </button>
        </div>
    );

}

export default JoinGameButton;