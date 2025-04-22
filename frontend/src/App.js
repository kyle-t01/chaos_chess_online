
import './App.css';
import ChessBoard from './components/ChessBoard';
import JoinGameButton from './components/JoinGameButton';


function App() {
  return (
    <div className="App">
      <JoinGameButton />
      <ChessBoard />
    </div>
  );
}

export default App;
