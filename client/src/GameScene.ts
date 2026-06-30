import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    private player!: Phaser.GameObjects.Rectangle;
    private targetX: number = window.innerWidth / 2;
    private targetY: number = window.innerHeight / 2;

    constructor() {
        super('GameScene');
    }

    create() {
        // Load saved state for offline mode
        const savedState = localStorage.getItem('playerState');
        if (savedState) {
            const { x, y } = JSON.parse(savedState);
            this.targetX = x;
            this.targetY = y;
        }

        // Create player dot (Slime)
        this.player = this.add.rectangle(this.targetX, this.targetY, 40, 40, 0x00ff00);
        
        // Touch to move (Mobile support)
        this.input.on('pointerdown', (pointer: Phaser.Input.Pointer) => {
            this.targetX = pointer.worldX;
            this.targetY = pointer.worldY;
            this.saveState();
        });
        
        // Drag to move continuously
        this.input.on('pointermove', (pointer: Phaser.Input.Pointer) => {
            if (pointer.isDown) {
                this.targetX = pointer.worldX;
                this.targetY = pointer.worldY;
                this.saveState();
            }
        });
    }

    update() {
        // Smooth movement (Delta movement)
        this.player.x += (this.targetX - this.player.x) * 0.1;
        this.player.y += (this.targetY - this.player.y) * 0.1;
    }

    private saveState() {
        localStorage.setItem('playerState', JSON.stringify({ x: this.targetX, y: this.targetY }));
    }
}
