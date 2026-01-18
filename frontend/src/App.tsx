import { useEffect, useState } from 'react';
import { gameSocket } from './api/GameSocket';
import { useGameState } from './store/GameContext';
import GameMap from './components/GameMap';
import { GameInterface } from './components/GameInterface';

interface Toast {
    id: number;
    message: string;
    type: 'error' | 'info';
}

function App() {
  const { state, dispatch } = useGameState();
  
  const [nickname, setNickname] = useState("Hero");
  const [selectedClass, setSelectedClass] = useState("");
  const [joinId, setJoinId] = useState("");
  
  const [toasts, setToasts] = useState<Toast[]>([]);

  const showToast = (msg: string, type: 'error' | 'info' = 'info') => {
      const id = Date.now();
      setToasts(prev => [...prev, { id, message: msg, type }]);
      setTimeout(() => {
          setToasts(prev => prev.filter(t => t.id !== id));
      }, 3000);
  };

  useEffect(() => {
    gameSocket.onEvent((eventWrapper: any) => {
        
        if (eventWrapper.eventType === 'state_snapshot') {
            dispatch({ type: 'STATE_SNAPSHOT', payload: eventWrapper.payload });
            showToast("World Loaded", 'info');
        } 
        else if (eventWrapper.eventType === 'error') {
             showToast(eventWrapper.payload.message, 'error');
        } 
        else if (eventWrapper.eventType === 'SOCKET_DISCONNECTED') {
            dispatch({ type: 'SOCKET_DISCONNECTED' });
            showToast("Connection Lost", 'error');
        }
        else {
            dispatch({ type: 'GAME_EVENT', payload: eventWrapper });
        }
    });

    gameSocket.activate();
    dispatch({ type: 'SOCKET_CONNECTED' });

    const timer = setTimeout(() => {
        gameSocket.requestPlayerClasses();
    }, 1000);

    return () => clearTimeout(timer);
  }, [dispatch]);

  useEffect(() => {
      if (state.sessionId) {
          console.log("Subscribing to session:", state.sessionId);
          gameSocket.subscribeToSession(state.sessionId);
          
          setTimeout(() => {
              gameSocket.requestSessionState(state.sessionId!);
          }, 500);
      }
  }, [state.sessionId]);

  useEffect(() => {
      if (state.playerClasses.length > 0 && !selectedClass) {
          setSelectedClass(state.playerClasses[0].templateId);
      }
  }, [state.playerClasses]);

  const handleCreate = () => {
      if (!selectedClass) return showToast("Select a class!", 'error');
      if (!nickname) return showToast("Enter nickname!", 'error');
      gameSocket.createSession(nickname, selectedClass);
  };

  const handleJoin = () => {
      if (!selectedClass) return showToast("Select a class!", 'error');
      if (!joinId) return showToast("Enter Session ID!", 'error');
      gameSocket.joinSession(joinId, nickname, selectedClass);
  };

  const copyId = () => {
      if(state.sessionId) {
          navigator.clipboard.writeText(state.sessionId);
          showToast("ID Copied!", 'info');
      }
  }

  return (
    <div style={{ 
        width: '100vw', 
        height: '100vh', 
        position: 'relative', 
        overflow: 'hidden', 
        background: '#111', 
        fontFamily: 'monospace', 
        color: '#eee' 
    }}>
      
      <div style={{ 
          position: 'absolute', 
          top: 20, 
          left: '50%', 
          transform: 'translateX(-50%)', 
          zIndex: 9999, 
          display: 'flex', 
          flexDirection: 'column', 
          gap: 10, 
          pointerEvents: 'none',
          width: 'max-content'
      }}>
          {toasts.map(t => (
              <div key={t.id} style={{
                  background: t.type === 'error' ? 'rgba(220, 53, 69, 0.9)' : 'rgba(40, 167, 69, 0.9)',
                  color: 'white', 
                  padding: '10px 20px', 
                  borderRadius: 4, 
                  boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
                  fontSize: '14px',
                  fontWeight: 'bold',
                  textAlign: 'center',
                  animation: 'fadeIn 0.3s'
              }}>
                  {t.type === 'error' ? '⚠️ ' : 'ℹ️ '} {t.message}
              </div>
          ))}
      </div>

      {state.sessionId ? (
          <>
              <div style={{ width: '100%', height: '100%', position: 'absolute', top: 0, left: 0 }}>
                  {state.map ? <GameMap /> : <div style={centerStyle}>Loading World...</div>}
              </div>

              <GameInterface />
              
              <div style={{ 
                  position: 'absolute', 
                  top: 10, 
                  right: 10, 
                  background: 'rgba(0,0,0,0.6)', 
                  padding: '5px 10px', 
                  borderRadius: 4, 
                  fontSize: 11, 
                  zIndex: 100,
                  display: 'flex',
                  alignItems: 'center'
              }}>
                  <span style={{opacity: 0.7, marginRight: 5}}>ID: {state.sessionId}</span>
                  <button 
                    onClick={copyId} 
                    style={{
                        cursor: 'pointer', 
                        background: 'transparent', 
                        border: 'none', 
                        color: '#4CAF50', 
                        fontSize: '14px'
                    }}
                    title="Copy Session ID"
                  >
                      📋
                  </button>
              </div>
          </>
      ) : (
          <div style={{ ...centerStyle, flexDirection: 'column', gap: 20, height: '100%' }}>
              <h1 style={{ color: '#f0a500', fontSize: '3rem', margin: 0, textShadow: '0 0 10px rgba(240, 165, 0, 0.5)' }}>
                  Dungeon Crawler
              </h1>
              
              <div style={{ 
                  background: '#222', 
                  padding: 30, 
                  borderRadius: 10, 
                  border: '1px solid #444', 
                  width: 300, 
                  display: 'flex', 
                  flexDirection: 'column', 
                  gap: 15,
                  boxShadow: '0 10px 30px rgba(0,0,0,0.5)'
              }}>
                  
                  <div>
                      <label style={{display: 'block', marginBottom: 5, fontSize: 12, color: '#888'}}>Nickname</label>
                      <input 
                          value={nickname} 
                          onChange={e => setNickname(e.target.value)}
                          style={inputStyle}
                      />
                  </div>

                  <div>
                      <label style={{display: 'block', marginBottom: 5, fontSize: 12, color: '#888'}}>Class</label>
                      <select 
                          value={selectedClass} 
                          onChange={e => setSelectedClass(e.target.value)}
                          style={inputStyle}
                      >
                          {state.playerClasses.length === 0 && <option>Loading classes...</option>}
                          {state.playerClasses.map(c => (
                              <option key={c.templateId} value={c.templateId}>{c.name}</option>
                          ))}
                      </select>
                  </div>

                  <hr style={{border: '0', borderTop: '1px solid #444', width: '100%', margin: '10px 0'}}/>

                  <button onClick={handleCreate} style={btnPrimary}>
                        Create Session
                  </button>

                  <div style={{display: 'flex', gap: 5}}>
                      <input 
                          placeholder="Session ID to join" 
                          value={joinId}
                          onChange={e => setJoinId(e.target.value)}
                          style={{...inputStyle, flex: 1}}
                      />
                      <button onClick={handleJoin} style={btnSecondary}>Join</button>
                  </div>
                  
                  <div style={{fontSize: 10, color: '#555', textAlign: 'center', marginTop: 10}}>
                      Server Status: {state.isConnected ? <span style={{color:'#4CAF50'}}>Online</span> : <span style={{color:'#F44336'}}>Offline</span>}
                  </div>
              </div>
          </div>
      )}
    </div>
  );
}

const centerStyle: React.CSSProperties = {
    display: 'flex', alignItems: 'center', justifyContent: 'center'
};

const inputStyle: React.CSSProperties = {
    width: '100%', padding: '10px', background: '#333', border: '1px solid #555', color: 'white', borderRadius: 4, boxSizing: 'border-box', outline: 'none'
};

const btnPrimary: React.CSSProperties = {
    padding: '12px', background: '#f0a500', color: 'black', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold', fontSize: '14px', width: '100%'
};

const btnSecondary: React.CSSProperties = {
    padding: '10px', background: '#444', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold'
};

export default App;