import React, { useEffect, useRef } from 'react';
import Phaser from 'phaser';
import GameScene from './GameScene';
import './App.css';

const App: React.FC = () => {
    const gameContainerRef = useRef<HTMLDivElement>(null);
    const gameInstanceRef = useRef<Phaser.Game | null>(null);

    useEffect(() => {
        if (gameContainerRef.current && !gameInstanceRef.current) {
            const config: Phaser.Types.Core.GameConfig = {
                type: Phaser.AUTO,
                width: window.innerWidth,
                height: window.innerHeight,
                parent: gameContainerRef.current,
                backgroundColor: '#282c34',
                scale: {
                    mode: Phaser.Scale.RESIZE,
                    autoCenter: Phaser.Scale.CENTER_BOTH
                },
                scene: [GameScene]
            };

            gameInstanceRef.current = new Phaser.Game(config);
        }

        return () => {
            if (gameInstanceRef.current) {
                gameInstanceRef.current.destroy(true);
                gameInstanceRef.current = null;
            }
        };
    }, []);

    return (
        <div className="App">
            <div ref={gameContainerRef} style={{ width: '100vw', height: '100vh', overflow: 'hidden' }} />
        </div>
    );
};

export default App;
