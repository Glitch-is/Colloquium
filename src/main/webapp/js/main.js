// Websocket URI
var wsUri = getRootUri();

// Websocket connection
function getRootUri() {
    return "ws://colloquium.glitch.is"; // Main deployment
    //return "ws://localhost:8080"; // For developing purposes
}

// Vars
var colors = ["aqua", "aquamarine", "blue", "blueviolet", "brown", "chartreuse", "chocolate", "cornflowerblue", "bisque", "crimson", "darkcyan", "darkgoldenrod", "darkgreen", "darkmagenta", "darkred", "darkorchid", "darksalmon", "darkseagreen", "darkslategray", "deeppink", "gold", "white", "greenyellow", "green", "hotpink", "indigo", "khaki", "lightblue", "orangered", "red", "saddlebrown"];
var colorNicks = {};
var chan = "#main";
var help = false;
var polio = false;
var editors = {};

addEditor("main");

// Stuff
function bindUi(){
	// When click, focus on messagebox
	$(".chat").on("click focus", function() {
		setTimeout(function(){$(".input").focus()},1);
	});

	// Enter button, send message
	$("body").on("keydown", ".input", function(e){
		// If ENTER
		if(e.which === 13)
		{
			// Get message, make sure it's not a command
			var mes = $(this).val();
			if(mes[0] === "/")
			{
				command(mes);
			}

			else
			{
				if(chan[0] === "#")
				{
					//Channel message
					doSend("message", chan.slice(1),  "\"" + mes + "\"");
				}
				else
				{
					//Private Message
					doSend("private", chan,  "\"" + mes + "\"");
				}
			}
			// Clear inputbox
			$(".input").val("");
		}
	});

	// Editor sending, whenever input is detected
	$("body").on("keyup", "textarea", function(e){
		// $(this).val().split("\n").join("\" , \"")
		//alert(editor.getValue());
		doSend("editor", (chan[0] === "#") ? chan.slice(1) : chan, "[\""+ editors[chan.slice(1)].getValue().split("\n").join("\" , \"") + "\"]");
	});

	// When clicked on .chan class, focus on messagebox
	$("body").on("click", ".chan", function(e)
	{
		chan = $(this).text();
		editors[chan.slice(1)].refresh();
		setTimeout(function(){$(".input").focus()},1);
	});

	// Close Help click
	$("body").on("click", ".loka", function(e)
	{
		$('.help').css({'top':'-500%','left': '0'});
	});

	// Close help outside click
	$("body").click(function(e){
		if(help){
			if (!$('.help').is(e.target) // if the target of the click isn't the container...
		        && $('.help').has(e.target).length === 0) // ... nor a descendant of the container
		    {
		        $('.help').css({'top':'-50%','left': '0'});
		    }
		}
	});

	// When window resizes, fix height
	$(window).on("resize", function(){
		fixHeight();
	});
}

// Timestamp
function getTime() {
    var now     = new Date();
    var hour    = now.getHours();
    var minute  = now.getMinutes();
    var second  = now.getSeconds();

    if(hour.toString().length === 1) {
        var hour = '0'+hour;
    }
    if(minute.toString().length === 1) {
        var minute = '0'+minute;
    }
    if(second.toString().length === 1) {
        var second = '0'+second;
    }
    var time = hour+':'+minute+':'+second;
    return time;
}

// Command parser (/[COMMAND])
function command(com)
{
	switch(com.split(" ")[0].slice(1).toLowerCase())
	{
		// Change nickname
		case "nick":
			doSend("nick", "", com.split(" ")[1]);
		break;

		// Me command
		case "me":
			doSend("action", chan, com.split(" ").slice(1).join(" "));
		break;

		// Join
		case "join":
			var cName = com.split(" ")[1];
			cName = (cName[0] === "#" ? cName.slice(1) : cName);
			doSend("join", "", cName);

			// Create new tab
			$(".tabs").append($("<dd><a class='chan' href='#"+cName+"'>#"+cName+"</a></dd>"));
			$(".tabs-content").append($('<div class="content" id="'+cName+'">\
											<div class="large-6 columns chat">\
												<div class="large-10 columns no-sides">\
													<div id="messages-'+cName+'" class="large-12 columns messages no-sides"></div>\
												<div class="large-12 columns no-sides">\
													<input type="text" class="input" />\
												</div>\
											</div>\
											<div class="large-2 columns">\
												<div class="nicklist" id="nicklist-'+cName+'"></div>\
											</div>\
										</div>\
										<div class="large-6 columns">\
											<textarea class="editor" id="editor-'+cName+'"></textarea>\
										</div>\
									</div>'));
			addEditor(cName);
			fixHeight();
		break;

		//Message
		case "msg":
		    doSend("private", com.split(" ")[1], "\"" + com.split(" ").slice(2).join(" ") + "\"");
		break;

		// Leave
		case "leave":
			doSend("leave", "", com.split(" ")[1]);
			// remove tab
		break;

		// Help
		case "commands":
		case "info":
		case "?":
		case "man":
		case "help":
			$('.help').css({'top':'30%','left': '20%'});
			help = true;
		break;
	}
}

// Check your privileges m8
function privilege( p)
{
	switch(p)
	{
		case "Owner":
			return "<span style='background: #00FF00; color: #000000; -moz-border-radius:4px 0 0 4px; border-radius:4px 0 0 4px;'>~</span>";
		case "Moderator":
			return "<span style='background: #FF00FF; color: #000000; -moz-border-radius:4px 0 0 4px; border-radius:4px 0 0 4px;'>@</span>";
		case "Voice":
			return "<span style='background: #0000FF; color: #000000; -moz-border-radius:4px 0 0 4px; border-radius:4px 0 0 4px;'>+</span>";
		default:
			return "";
	}
}

// Initialize
function init() {
	var col = localStorage.getItem("colors");
	var nick = localStorage.getItem("nick");
	var chans = localStorage.getItem("chans");
	if(col !== null)
		colorNicks = JSON.parse(col);
	//if(nick !== null)

	setTimeout(function(){$(".input").focus()},1);
	websocket = new WebSocket(wsUri);

	websocket.onopen = function (evt) {
		onOpen(evt);
	};
	websocket.onclose = function (evt) {
		onClose(evt);
	};
	websocket.onmessage = function (evt) {
		onMessage(evt);
	};
	websocket.onerror = function (evt) {
		onError(evt);
	};
}

// THIS IS NOT ACCEPTABLE
function onOpen(evt) {
}

// Reveive messages from server
function onMessage(evt) {
	console.log("IN: " + evt.data);
	var o = JSON.parse(evt.data);

	/*JSON markup:
	o.head     = Type of server response
	o.message  = contents of response
	o.chatroom = destination of response
	*/

	// If no chatroom in response, chatroom = current chatroom
	if(o.chatroom === "")
		o.chatroom = chan;

	//Head
	switch(o.head)
	{
		// Nickname list
		case "nicklist":
			// Empty nicklist
			$("#nicklist-" + o.chatroom).html("");

			// Populate nicklist
			for(nick in o.message)
			{
				nick = o.message[nick];

				// Apply random color
				if(colorNicks[nick[0]] === undefined)
				{
					colorNicks[nick[0]] = colors[Math.floor(Math.random() * colors.length)];
				}

				// Keep name colors
				localStorage.setItem("colors", JSON.stringify(colorNicks));
				// Add user to line
				var line = $("<div class='line'>");
				line.append(privilege(nick[1]) + "<span style='color:"+colorNicks[nick[0]]+";'>" + nick[0] + "</span>");
				// Append line to nicklist
				$("#nicklist-" + o.chatroom).append(line);
			}
			break;

		// Editor
		case "editor":
			editors[o.chatroom].setValue(o.message.join("\n"));
			/*
			// Editor
			var ed = $("#editor-"+o.chatroom);
			// Get cursor position
			var position = ed.prop("selectionStart");
			// Set the editor value to the new contents of the editor
			ed.val(o.message.join("\n"));
			// Position the editor cursor
			document.getElementById("editor-" + o.chatroom).setSelectionRange(position, position);
			*/
			break;

		// Message of the day or Server messages
		case "server":
		case "motd":
			writeToChan(o.chatroom, getTime() + " " + o.message);
			break;

		// Private messages
		case "private":
			// Chatroom = username
			var username = o.chatroom;

			// Check if there is already a private session in progress with the desired user
			var found = false;
			$(".chan").each(function(ind){
				if($(this).text() === username)
					found = true;
			});

			// If there isn't a session, create it
			if(!found)
			{
				$(".tabs").append($("<dd><a class='chan' href='#"+username+"'>"+username+"</a></dd>"));
				$(".tabs-content").append($('<div class="content" id="'+username+'">\
												<div class="large-6 columns chat">\
													<div class="large-10 columns no-sides">\
														<div id="messages-'+username+'" class="large-12 columns messages no-sides"></div>\
													<div class="large-12 columns no-sides">\
														<input type="text" class="input" />\
													</div>\
												</div>\
												<div class="large-2 columns">\
													<div class="nicklist" id="nicklist-'+username+'"></div>\
												</div>\
											</div>\
											<div class="large-6 columns">\
												<textarea class="editor" id="editor-'+username+'"></textarea>\
											</div>\
										</div>'));
				fixHeight();
			}
			// Write the message
			writeToChan(username, getTime() + " <span style='color: gray;'>&lt;</span> <span style='color: " + colorNicks[o.message.split(" ")[0]] + ";'><b>" + o.message.split(" ")[0] + "</b></span> <span style='color: gray;'>&gt;</span> " + o.message.split(" ").slice(1).join(" "));
			break;

		// /me command
		case "action":
			writeToChan(o.chatroom, getTime() + " <span style='color: #FF00FF'>*** " + o.message + "</span>" );
			break;

		// Marco-Polio
		case "polio":
			polio = true;
			break;

		// Normal message
		default:
			console.log(o.head);
			console.log(colorNicks);
			writeToChan(o.chatroom, getTime() + " <span style='color: gray;'>&lt;</span> <span style='color: " + colorNicks[o.head] + ";'><b>" + o.head + "</b></span> <span style='color: gray;'>&gt;</span> " + o.message);
			break;
	}
	// Linkify URLs
	$(".line").linkify();
}

// Error
function onError(evt) {
	writeToChan(chan, '<span style="color: red;">ERROR:</span> Connection to server <b><span style="color:red">[FAILED]</span></b>');
}

// Send to server
function doSend(head, chatroom, message) {
	console.log("OUT: {\"head\":\""+head+"\", \"chatroom\":\""+chatroom+"\", \"message\":"+message+"}");
	websocket.send("{\"head\":\""+head+"\", \"chatroom\":\""+chatroom+"\", \"message\":"+message+"}");
}

// Update chat
function writeToChan(chan, message) {
	chan = (chan[0] === "#" ? chan.slice(1) : chan);
	var line = document.createElement("div");
	line.style.wordWrap = "break-word";
	line.className = "line";
	$(line).append(message);
	$("#messages-"+chan).append(line);
	$("#messages-"+chan).scrollTop($("#messages-"+chan)[0].scrollHeight);
}

// == Fixes ==
// Auto-height
function fixHeight()
{
	$('.editor').css({'height':(($(window).height())-80)+'px'});
	$('.CodeMirror').css({'height':(($(window).height())-80)+'px'});
	$('.messages').css({'height':(($(window).height())-118)+'px'});
	$('.nicklist').css({'height':(($(window).height())-80)+'px'});
}

// Operation Marco-Polio Protocol MK3 Beta v0.8
/*
setTimeout(function() {
      doSend("marco");
      polio = false;
}, 5000);

setTimeout(function() {
      if(!polio){
      	init();
      }
}, 9000);*/

// Push editor
function addEditor(name)
{
	var newEditor = CodeMirror.fromTextArea(document.getElementById("editor-" + name), {
	    lineNumbers: true,
	    mode: "text/html",
	    matchBrackets: true,
	    theme: "the-matrix"
	});

	editors[name] = newEditor;
}

// MAIN
$(function() {
	bindUi();
	init();
	fixHeight();
});
