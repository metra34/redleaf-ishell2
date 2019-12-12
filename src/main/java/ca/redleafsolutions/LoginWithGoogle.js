function ${function-logout}() {
	gapi.auth.signOut();
	location.reload();
}

function ${function-login} () {
	var myParams = {
		'clientid' : '${clientid}',
		'cookiepolicy' : 'single_host_origin',
		'callback' : 'loginCallback',
		'approvalprompt' : 'force',
		'scope' : 'https://www.googleapis.com/auth/userinfo.email',
		'scope__' : 'https://www.googleapis.com/auth/plus.login',
		'scope_' : 'https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/plus.profile.emails.read'
	};
	gapi.auth.signIn(myParams);
}

function loginCallback(result) {
	// console.log("Result:", result);
	if (result['status']['signed_in']) {
		var request = gapi.client.plus.people.get({
			'userId' : 'me'
		});
		request.execute(function(resp) {
			gform.children[0].value = JSON.stringify(resp);
			gform.submit();
		});
	}
}

function onLoadCallback() {
	gapi.client.setApiKey('${apikey}');
	gapi.client.load('plus', 'v1', function() {
	});
}

var gform;

(function() {
	var po = document.createElement('script');
	po.type = 'text/javascript';
	po.async = true;
	po.src = 'https://apis.google.com/js/client.js?onload=onLoadCallback';
	var script = document.getElementsByTagName('script')[0];
	script.parentNode.insertBefore(po, script);
	
	// add a form to allow POST method submit
	gform = document.createElement('form');
	gform.action = "${callback-path}";
	gform.method = 'POST';
	var inp = document.createElement('input');
	inp.name = '${callback-param}';
	inp.type = 'hidden';
	gform.appendChild (inp);
})();
