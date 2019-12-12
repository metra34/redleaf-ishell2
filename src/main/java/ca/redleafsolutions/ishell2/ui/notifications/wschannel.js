var socket;

function log (str) {
	try {
		str = str.join (" ");
	} catch (e) {}
	console.log(new Date().getTime() % 100000, str);
}

$(document).ready(function() {
	var host = location.host;
	var lst = host.split(":");
	host = lst[0];
	var wsprotocol = "ws:";
#if (${channel.secureport()} > 0)
	if (location.protocol == "https:") {
		wsprotocol = "wss:";
	}
	var wsport = ${channel.getSecurePort()};
#else
	var wsport = ${channel.port()};
#end
	var sockuri = wsprotocol + "//" + host;
	if (wsport && (wsport != 80))
		sockuri += ":" + wsport;

	socket = new Socket(sockuri);
});

function handleUpdates(data) {
	console.log("function handleUpdates", data);
	var ids = {};
	for (var i in data) {
		var item = data[i];
		var id = item.id;
		if (id == undefined ) {
			handleEventUpdates (item);
		} else if (id == "*") {
			handleSpecialEvent(item);
		} else {
			var value = item.value;
			if (item.type == "table") {
				item.header = [];
				var s = "";
				for (var col in item.columns) {
					if (s.length > 0)
						s += "</th><th>";
					// because template must contain opening and closing <tr><th>..</th></tr>
					s += item.columns[col].title;
				}
				item.headers = s;
				agenta.attr[id].headers = s;

				if (item.key == "data")
					item.key = "rows";
				s = "";
				for (var row in value) {
					if (s.length > 0)
						s += "</td></tr><tr><td>";
					var index = 0;
					var line = "";
					for (var val in value[row]) {
						var fval = value[row][val];
						try {
							var formatter = item.columns[index].format;
							if ((formatter != null) && (formatter.length > 6)) {
								var custom = formatter.substr(0, 7);
								if (custom == "custom/") {
									func = formatter.substr(7);
									try {
										// this is to make sure that Strings are wrapped with quotation marks and objects are not;
										dummy = JSON.parse(fval);
									} catch (e) {
										fval = '"' + fval + '"';
									}
									fval = eval(func + "(" + fval + ")");
								}
							}
						} catch (e) {
						}

						if (line.length > 0)
							line += "</td><td>";
						line += fval;
						++index;
					}
					s += line;
				}
				value = s;
			} else {
				// if value is a function, execute it
				if ( typeof (value) == "function") {
					value = value();
				}

				// apply formatter if applicable
				for (var f in agenta.formatters) {
					var fitem = agenta.formatters[f];
					if (fitem.id == id) {
						if (fitem.attribute == item.key) {
							var fname = fitem.name;
							var frmt = window.formatter[fitem.name];
							if (frmt && ( typeof (frmt) == "function")) {
								value = frmt(value);
							}
						}
					}
				}
			}

			// apply to attributes
			if (agenta.attr[id]) {
				agenta.attr[id][item.key] = value;
			}
			ids[id] = null;
		}
	}

	// now traverse all changed components and re-render them
	for (var id in ids) {
		var attr = agenta.attr[id];
		var template = agenta.templates[id];
		if (template != null) {
			s = template.render(attr);
			$("#" + id).html(s);
			template.updateScript(id);
		}
	}
}

function handleSpecialEvent(data) {
	if (data.key == "reload") {
		location.reload();
		return;
	} else if (data.key.substring (0,"javascript:".length) != undefined ) {
		var func = data.key.substring ("javascript:".length);
		window[func] (data.value);
		return;
	}
}

function handleEventUpdates (data) {
	console.log (data);
}

function Socket(uri) {
	this.websocket = null;
	this.interval = null;
	if (uri != null) {
		this.open(uri);
	}
}
Socket.prototype.open = function(uri) {
	this.uri = uri;
	console.log(new Date().getTime() % 100000, "Openning WS on " + uri);
	websocket = new WebSocket(uri);
	websocket.onopen = function(event) {
		console.log(new Date().getTime() % 1000000, "WS opened");
		socket.send("Connected from " + location.href);
		this.interval = setInterval (function () {
			socket.send ("hearbeat");
		}, 1000*60*2);
	};
	
	websocket.onclose = function(event) {
		console.log(new Date().getTime() % 1000000, event.type);
		clearInterval (this.interval);
		this.interval = null;
		this.websocket = null;
	};
	websocket.onmessage = function(event) {
		console.log(new Date().getTime() % 1000000, event.type, event.data);
		handleUpdates([JSON.parse(event.data)]);
	};
	websocket.onerror = function(event) {
		console.log(new Date().getTime() % 1000000, event.type, event);
	};
};

Socket.prototype.close = function() {
	return websocket.close();
};
Socket.prototype.send = function(data) {
	console.log (new Date().getTime() % 1000000, "WS sending " + data);
	return websocket.send(data);
};
