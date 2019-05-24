Spacewar.menuState = function(game) {

}
var namePlayer;
var nameSala = " ";
var color = "#ffffff";
var style;
var colores= ["#E10000", "#EA00C3", "#10C5F5", "#10F510"];
var empezarBtn= false;
var crearBtn= true;
var unirseBtn= true;
var enPartida=false;
var enSala = false;

/*function pedirNombre(){
	do
	{
		namePlayer = prompt("Inserta el nombre de tu nave: ", "nave1"); //esta función nos permite pedir al usuario un nombre, que se guardará en la variable namePlayer.
	} while(namePlayer == " "|| namePlayer == "null");
	color = colores[Math.floor(Math.random() * 4)];
	style = { font: "bold 10px Arial", fill: color, boundsAlignH: "center", boundsAlignV: "middle" };
}*/

function pedirNombre(){
    do
    {
        namePlayer = prompt("Inserta el nombre de tu nave: ", "nave1"); //esta función nos permite pedir al usuario un nombre, que se guardará en la variable namePlayer.
       
    } while(namePlayer == " "|| namePlayer == "null" || namePlayer == ""|| namePlayer == null  );
   
    color = colores[Math.floor(Math.random() * 4)];
    style = { font: "bold 10px Arial", fill: color, boundsAlignH: "center", boundsAlignV: "middle" };
}

function nomSala(){
	
}
 
Spacewar.menuState.prototype = {


	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
		pedirNombre();
		
	},

	create : function() {
		//pedirNombre();
		if (typeof game.global.myPlayer.id == 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Forcing joining server...");
			}
		}
		let message = {
				event : 'JOIN',
				params : namePlayer,
				color : color
			}
		game.global.socket.send(JSON.stringify(message))
	},

	update : function() {
		if (typeof game.global.myPlayer.id !== 'undefined') {
			game.state.start('lobbyState')
		}
	}
}

