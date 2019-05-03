window.onload = function() {

	game = new Phaser.Game(1024, 600, Phaser.AUTO, 'gameDiv')

	// GLOBAL VARIABLES, Son variables globales para que aunque cambiemos de estado, sigan estando definidas.
	game.global = { //Lo usamos como un mapa, por eso se crean como arrays o como objetos, en funcion de como trabajaremos con ellos. Normalmente los jugadores con ids para usar arrays.
		FPS : 30,
		DEBUG_MODE : false,
		socket : null,
		myPlayer : new Object(),
		otherPlayers : [], 
		projectiles : []
	}

	// WEBSOCKET CONFIGURATOR
	game.global.socket = new WebSocket("ws://127.0.0.1:8080/spacewar") //Creamos la conexion. Si queremos conectar varios equipos, ponemos en la id la direccion del servidor. 
	
	game.global.socket.onopen = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection opened.')
			//Si ponemos console.dir nos imprime todo lo relacionado con el objeto
		}
	}

	game.global.socket.onclose = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection closed.')
		}
	}
	
	game.global.socket.onmessage = (message) => {
		var msg = JSON.parse(message.data)
		
		switch (msg.event) {
		case 'JOIN': //Se une una nave a la partida
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] JOIN message recieved')
				console.dir(msg)
			}
			game.global.myPlayer.id = msg.id
			game.global.myPlayer.shipType = msg.shipType //Como hay muchos tipos de naves, se indica el tipo para que todas las naves estén identificadas
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] ID assigned to player: ' + game.global.myPlayer.id)
			}
			break
		case 'NEW ROOM' : //Una partida como tal, configuracion del espacio de juego, nº jugadores, etc... Es donde tenemos que hacer toda la chicha
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] NEW ROOM message recieved')
				console.dir(msg)
			}
			game.global.myPlayer.room = {
					name : msg.room //Nombre de la habitacion o mapa
			}
			break
		case 'GAME STATE UPDATE' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] GAME STATE UPDATE message recieved')
				console.dir(msg)
			}
			if (typeof game.global.myPlayer.image !== 'undefined') { //Si la imagen del jugador no está creada
				for (var player of msg.players) {
					if (game.global.myPlayer.id == player.id) { //Si la id que estamos usando coincide con la id del array de jugadores
						game.global.myPlayer.image.x = player.posX
						game.global.myPlayer.image.y = player.posY
						game.global.myPlayer.image.angle = player.facingAngle
						game.global.myPlayer.vida = player.vida;
						
						game.global.myPlayer.liveSprite.x = player.posX - 15;
						game.global.myPlayer.liveSprite.y = player.posY
						game.global.myPlayer.liveSprite.angle = player.facingAngle
						game.global.myPlayer.liveSprite.anchor.setTo(0.1, 0.1);
						console.log(game.global.myPlayer.vida);
					} else {
						if (typeof game.global.otherPlayers[player.id] == 'undefined') { //Si hay otros jugadores distintos al de mi id que no estan definidos, lo creamos. 
							game.global.otherPlayers[player.id] = {
									image : game.add.sprite(player.posX, player.posY, 'spacewar', player.shipType)
							}
							game.global.otherPlayers[player.id].image.anchor.setTo(0.5, 0.5)
						} else { //Si el jugador ya existe
							game.global.otherPlayers[player.id].image.x = player.posX
							game.global.otherPlayers[player.id].image.y = player.posY
							game.global.otherPlayers[player.id].image.angle = player.facingAngle
						}
					}
				}
				
				for (var projectile of msg.projectiles) { //Aqui creamos los proyectiles. 
					if (projectile.isAlive) { //directamente los creo todos y los cargo, pero siendo invisibles. Cuando disparo, lo que hacemos es mostrarlas o quitarlas.
						game.global.projectiles[projectile.id].image.x = projectile.posX
						game.global.projectiles[projectile.id].image.y = projectile.posY
						if (game.global.projectiles[projectile.id].image.visible === false) {
							game.global.projectiles[projectile.id].image.angle = projectile.facingAngle //No se actualiza el angulo, porque los proyectiles van en linea recta. 
							game.global.projectiles[projectile.id].image.visible = true
						}
					} else {
						if (projectile.isHit) { //Si el proyectil ha golpeado algo. Habría que hacer comprobaciones de que se ha golpeado, un muro, otra nave...
							// we load explosion
							let explosion = game.add.sprite(projectile.posX, projectile.posY, 'explosion')
							explosion.animations.add('explosion')
							explosion.anchor.setTo(0.5, 0.5)
							explosion.scale.setTo(2, 2)
							explosion.animations.play('explosion', 15, false, true)
						}
						game.global.projectiles[projectile.id].image.visible = false
					}
				}
			}
			break
		case 'REMOVE PLAYER' : //se elimina un jugador, ya sea porque se ha ido de la partida, o porque se ha mierto
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] REMOVE PLAYER message recieved')
				console.dir(msg.players)
			}
			game.global.otherPlayers[msg.id].image.destroy() //Busca el id del jugador en el array, destruimos su posicion en el array y borramos su imagen del mapa.
			delete game.global.otherPlayers[msg.id]
		default :
			console.dir(msg)
			break
		}
	}

	// PHASER SCENE CONFIGURATOR
	game.state.add('bootState', Spacewar.bootState)
	game.state.add('preloadState', Spacewar.preloadState)
	game.state.add('menuState', Spacewar.menuState)
	game.state.add('lobbyState', Spacewar.lobbyState)
	game.state.add('matchmakingState', Spacewar.matchmakingState)
	game.state.add('roomState', Spacewar.roomState)
	game.state.add('gameState', Spacewar.gameState)

	game.state.start('bootState')

}