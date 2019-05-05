Spacewar.preloadState = function(game) {

}

Spacewar.preloadState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **PRELOAD** state");
		}
	},

	preload : function() {
		game.load.atlas('spacewar', 'assets/atlas/spacewar.png',
				'assets/atlas/spacewar.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.atlas('explosion', 'assets/atlas/explosion.png',
				'assets/atlas/explosion.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.image('live', 'assets/images/live.png')
	},

	create : function() { //Primero se cargan las imagenes en el preload, y luego ya cambia al estado menuState
		game.state.start('menuState')
	},

	update : function() {

	}
}