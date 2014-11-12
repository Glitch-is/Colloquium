var wsUri = getRootUri();

// Websocket connection
function getRootUri() {
    return "ws://colloquium.glitch.is";
    //return "ws://localhost:8080";
}

// Vars
var chan = "main";
var help = false;

// Stuff
function bindUi(){
	$(".chat").on("click focus", function() {
		setTimeout(function(){$(".input").focus()},1);
	});

	$("body").on("keydown", ".input", function(e){
		if(e.which === 13)
		{
			var mes = $(this).val();
			if(mes[0] === "/")
				command(mes);
			else
			{
				doSend("message", chan,  "\"" + mes + "\"");
			}
			$(".input").val("");
		}
	});

	$("body").on("input", ".editor", function(e){
		doSend("editor", chan, "[\""+ $(this).val().split("\n").join("\" , \"") + "\"]");
	});

	$("body").on("click", ".chan", function(e)
	{
		chan = $(this).text();
		setTimeout(function(){$(".input").focus()},1);
	});

	// Close Help click
	$("body").on("click", ".loka", function(e)
	{
		$('.help').css({'top':'-50%','left': '0'});
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

// Command parser
function command(com)
{
	switch(com.split(" ")[0].slice(1).toLowerCase())
	{
		case "nick":
			doSend("nick", "", com.split(" ")[1]);
		break;
		case "me":
			doSend("action", chan, com.split(" ").slice(1).join(" "));
		break;
		case "join":
			var cName = com.split(" ")[1];
			cName = (cName[0] === "#" ? cName.slice(1) : cName);
			doSend("join", "", cName);
			$(".tabs").append($("<dd><a class='chan' href='#"+cName+"'>"+cName+"</a></dd>"));
			$(".tabs-content").append($('<div class="content" id="'+cName+'"> <div class="large-6 columns chat"> <div class="large-10 columns no-sides"> <div id="messages-'+cName+'" class="large-12 columns messages no-sides"> </div> <div class="large-12 columns no-sides"> <input type="text" class="input" /> </div> </div> <div class="large-2 columns"> <div class="nicklist" id="nicklist-'+cName+'"> </div> </div> </div> <div class="large-6 columns"> <textarea class="editor" id="editor-'+cName+'"></textarea> </div> </div>'));
			fixHeight();
		break;
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

function onOpen(evt) {
}

// Reveive messages from server
function onMessage(evt) {
	console.log("IN: " + evt.data);
	var o = JSON.parse(evt.data);
	if(o.chatroom === "")
		o.chatroom = chan;
	switch(o.head)
	{
		case "nicklist":
			$("#nicklist-" + o.chatroom).html("");
			for(nick in o.message)
			{
				var line = $("<div class='line'>");
				line.append(privilege(o.message[nick][1]) + o.message[nick][0]);
				$("#nicklist-" + o.chatroom).append(line);
			}
			break;
		case "editor":
			var ed = $("#editor-"+o.chatroom);
			var position = ed.prop("selectionStart");
			ed.val(o.message.join("\n"));
			document.getElementById("editor-" + o.chatroom).setSelectionRange(position, position);
			break;
		case "motd":
			writeToChan(o.chatroom, getTime() + " " + o.message);
			break;
		case "server":
			writeToChan(o.chatroom, getTime() + " " + o.message);
			break;
		case "action":
			writeToChan(o.chatroom, getTime() + " <span style='color: #FF00FF'>*** " + o.message + "</span>" );
			break;
		case "ping":
			doSend("pong", "", "");
		break;
		default:
			writeToChan(o.chatroom, getTime() + " <span style='color: gray;'>&lt;</span> <span style='color: aqua;'><b>" + o.head + "</b></span> <span style='color: gray;'>&gt;</span> " + o.message);
			break;
	}
	$(".line").linkify();
}

// Error
function onError(evt) {
	writeToChan("main", '<span style="color: red;">ERROR:</span> Connection to server <b><span style="color:red">[FAILED]</span></b>');
}

// Send to server
function doSend(head, chatroom, message) {
	console.log("OUT: {\"head\":\""+head+"\", \"chatroom\":\""+chatroom+"\", \"message\":"+message+"}");
	websocket.send("{\"head\":\""+head+"\", \"chatroom\":\""+chatroom+"\", \"message\":"+message+"}");
}

// Update chat
function writeToChan(chan, message) {
	var line = document.createElement("div");
	line.style.wordWrap = "break-word";
	line.className = "line";
	$(line).append(message);
	$("#messages-"+chan).append(line);
	$("#messages-"+chan).scrollTop($("#messages-"+chan)[0].scrollHeight);
}

// Fixes
function fixHeight()
{
	$('.editor').css({'height':(($(window).height())-80)+'px'});
	$('.messages').css({'height':(($(window).height())-118)+'px'});
	$('.nicklist').css({'height':(($(window).height())-80)+'px'});
}

// MAIN
$(function() {
	bindUi();
	init();
	fixHeight();
});
