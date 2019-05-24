Spacewar.gameState = function(game) {
	this.bulletTime
	this.fireBullet
	this.numStars = 100 // Should be canvas size dependant
	this.maxProjectiles = 800 // 8 per player
}
//var text;
Spacewar.gameState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **GAME** state");
		}
	},

	preload : function() {	
		// We create a procedural starfield background
		for (var i = 0; i < this.numStars; i++) {
			let sprite = game.add.sprite(game.world.randomX,
					game.world.randomY, 'spacewar', 'staralpha.png');
			let random = game.rnd.realInRange(0, 0.6);
			sprite.scale.setTo(random, random)
		}

		// We preload the bullets pool
		game.global.proyectiles = new Array(this.maxProjectiles)
		for (var i = 0; i < this.maxProjectiles; i++) {
			game.global.projectiles[i] = {
				image : game.add.sprite(0, 0, 'spacewar', 'projectile.png')
			}
			game.global.projectiles[i].image.anchor.setTo(0.5, 0.5)
			game.global.projectiles[i].image.visible = false
		}

		// we load a random ship
		let random = [ 'blue', 'darkgrey', 'green', 'metalic', 'orange',
				'purple', 'red' ]
		let randomImage = random[Math.floor(Math.random() * random.length)]
				+ '_0' + (Math.floor(Math.random() * 6) + 1) + '.png';
		
		game.global.myPlayer.image = game.add.sprite(0, 0, 'spacewar',
				game.global.myPlayer.shipType);
		
		game.global.myPlayer.image.anchor.setTo(0.5, 0.5)
		
		game.global.myPlayer.liveSprite = game.add.sprite(0, 0, 'live');
		game.global.myPlayer.liveSprite.scale.setTo(0.02, 0.02);
		
		game.global.myPlayer.liveSprite2 = game.add.sprite(0, 0, 'live');
		game.global.myPlayer.liveSprite2.scale.setTo(0.02, 0.02);
		
		game.global.myPlayer.liveSprite3 = game.add.sprite(0, 0, 'live');
		game.global.myPlayer.liveSprite3.scale.setTo(0.02, 0.02);
		
		game.global.myPlayer.text = game.add.text(0, 0, namePlayer, style);
		game.global.myPlayer.text.style.fill = color;
		
		game.global.myPlayer.push = game.add.text(0,0, "100", style);
		game.global.myPlayer.push.style.fill = color;
		
		game.global.myPlayer.puntos = game.add.text(0,0, "0", style);
		game.global.myPlayer.puntos.style.fill = color;﻿
		
		console.log("preload image");
	},

	create : function() {
		this.bulletTime = 0 //el istante inicial de la bala es 0. 
		this.fireBullet = function() {
			if (game.time.now > this.bulletTime) {
				this.bulletTime = game.time.now + 250; //Podemos disparar 4 veces por segundo
				// this.weapon.fire()
				return true
			} else {
				return false
			}
		}

		this.wKey = game.input.keyboard.addKey(Phaser.Keyboard.UP);
		this.sKey = game.input.keyboard.addKey(Phaser.Keyboard.DOWN);
		this.aKey = game.input.keyboard.addKey(Phaser.Keyboard.LEFT);
		this.dKey = game.input.keyboard.addKey(Phaser.Keyboard.RIGHT);
		this.spaceKey = game.input.keyboard.addKey(Phaser.Keyboard.CONTROL);
		this.shiftKey = game.input.keyboard.addKey(Phaser.Keyboard.SHIFT);

		// Stop the following keys from propagating up to the browser
		//game.input.keyboard.addKeyCapture([ Phaser.Keyboard.W,
		//		Phaser.Keyboard.S, Phaser.Keyboard.A, Phaser.Keyboard.D,
		//		Phaser.Keyboard.SPACEBAR ]);

		game.camera.follow(game.global.myPlayer.image);
	},

	update : function() {
		if(enPartida){
			let msg = new Object()
			msg.event = 'UPDATE MOVEMENT'
				
			msg.nomSal = nameSala				//me parece que sobra
	
			msg.movement = {
				thrust : false,
				brake : false,
				rotLeft : false,
				rotRight : false
			}
			
			msg.push = false
			msg.bullet = false
	
			if (this.wKey.isDown)
				msg.movement.thrust = true;
			if (this.sKey.isDown)
				msg.movement.brake = true;
			if (this.aKey.isDown)
				msg.movement.rotLeft = true;
			if (this.dKey.isDown)
				msg.movement.rotRight = true;
			if (this.spaceKey.isDown) {
				msg.bullet = this.fireBullet()
			}
			if (this.shiftKey.isDown) {
				msg.push = true;
			}
	
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Sending UPDATE MOVEMENT message to server")
			}
			game.global.socket.send(JSON.stringify(msg))
		}
	}
}