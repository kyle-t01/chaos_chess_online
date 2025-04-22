import { GlobalVars } from "../context/GlobalContext";
import "../chessboard.css"

const ChessBoard = () => {
    // global state
    const { gameState } = GlobalVars()
    console.log(gameState)
    // variables derived from global gameState
    const numRows = gameState?.dimension.row ?? 6
    const numCols = gameState?.dimension.col ?? 6
    const size = numRows * numRows
    const board = gameState?.currentState.board.board ?? []


    const renderBoard = () => {
        if (!board || board.length == 0) return;
        const rows = []
        for (let r = numRows - 1; r >= 0; r--) {
            rows.push(renderRow(r))
        }
        return rows
    }

    const renderRow = (r) => {
        const cols = []

        for (let c = 0; c < numCols; c++) {
            cols.push(renderSquare(c, r))
        }
        return (
            <div className="row" key={r}>
                {cols}
            </div>
        )

    }


    const renderSquare = (col, row) => {
        // assume that idx always within bounds of board
        const idx = col + row * numCols
        const piece = board[idx]
        return (
            <div className="square" key={idx}>
                {piece}
            </div>
        );
    }



    return (
        <div className="chess-board">
            {renderBoard()}
        </div>
    );
}

export default ChessBoard;