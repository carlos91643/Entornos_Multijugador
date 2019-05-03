Spacewar.menuState = function(game) {

}
var namePlayer;

 function pedirNombre(){
	do
	{
		namePlayer = prompt("Inserta el nombre de tu nave: ", "nave1"); //esta función nos permite pedir al usuario un nombre, que se guardará en la variable namePlayer.
	} while(name == "nave1"|| name == "null");
}
 
Spacewar.menuState.prototype = {


	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
		
	},

	create : function() {
		pedirNombre();
		if (typeof game.global.myPlayer.id == 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Forcing joining server...");
			}
		}
		let message = {
				event : 'JOIN',
				params : namePlayer
			}
			game.global.socket.send(JSON.stringify(message))
	},

	update : function() {
		if (typeof game.global.myPlayer.id !== 'undefined') {
			game.state.start('lobbyState')
		}
	}
}

