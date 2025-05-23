import { GlobalVars } from "../context/GlobalContext";
import { useState } from "react";
import "../chessboard.css"

const ChessBoard = () => {
    // global state
    const { gameState, sendEvent, validActions, setValidActions } = GlobalVars()
    // move to global vars later so it can handle sending events
    const [clickedIdx, setClickedIdx] = useState(null)

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

    const getIndex = (col, row) => {
        return col + row * numCols
    }

    const getVector2DfromIndex = (idx) => {
        const col = idx % numCols;
        const row = Math.floor(idx / numCols);
        return { col, row };
    };

    const renderSquare = (col, row) => {
        // assume that idx always within bounds of board
        const idx = col + row * numCols
        const piece = board[idx]
        if (validActions.includes(idx)) {
            return (
                <div className="square" key={idx} onClick={() => handleSquareClicked(col, row)}>
                    <span>
                        {renderPiece(piece)} x
                    </span>

                </div>
            );
        }
        return (
            <div className="square" key={idx} onClick={() => handleSquareClicked(col, row)}>
                <span>
                    {renderPiece(piece)}
                </span>

            </div>
        );
    }

    const handleSquareClicked = (col, row) => {
        console.log(`clicked (col=${col}, row=${row})`)

        const idx = getIndex(col, row)
        const piece = board[idx]
        // if clicked on empty square when previous square was empty, return 
        if (piece === ' ' && clickedIdx == null) return;
        // if clicked the same square, hide valid moves
        if (clickedIdx == idx) {
            setClickedIdx(null)
            setValidActions([])
            return
        }

        // now we have defs picked a new square, we are moving this piece if within valid actions
        if (validActions.includes(idx)) {
            // second click on diff square, move there
            const from = getVector2DfromIndex(clickedIdx)
            const to = getVector2DfromIndex(idx)
            const data = `${from.col},${from.row} ${to.col},${to.row}`
            sendEvent("MOVE", data)
            setClickedIdx(null);
            setValidActions([])
            return
        }



        // first click, show me legal actions


        setClickedIdx(idx)
        sendEvent("LEGAL_ACTIONS", idx)

    }

    const toVector2D = (col, row) => {
        return { col: col, row: row }
    }

    const renderPiece = (c) => {
        if (!c || c === ' ') return;
        const isLower = c === c.toLowerCase()
        const color = isLower ? 'red' : 'black';
        const style = {
            color: color,
            border: `2px solid ${color}`,
            borderRadius: '50%',
            width: '40px',
            height: '40px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '22px',
            fontWeight: 'bold',
            backgroundColor: 'white'
        }


        switch (c.toUpperCase()) {
            case 'P': return <span style={style}>♟</span>
            case 'B': return <span style={style}>♝</span>
            case 'N': return <span style={style}>♞</span>
            case 'R': return <span style={style}>♜</span>
            case 'Q': return <span style={style}>♛</span>
            case 'K': return <span style={style}>♚</span>
            case 'Z': return <span style={style}>卒</span>
            case 'S': return <span style={style}>士</span>
            case 'X': return <span style={style}>象</span>
            case 'M': return <span style={style}>馬</span>
            case 'J': return <span style={style}>車</span>
            case 'C': return <span style={style}>砲</span>
            case 'G': return <span style={style}>將</span>
            case '.' || ' ': return <span></span>
            default: return <span style={style}>{c}</span>
        }
    }

    return (
        <div className="chess-board">
            {renderBoard()}
        </div>
    );
}

export default ChessBoard;