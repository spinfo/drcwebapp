var locked = false;
var lang;
var phrase;

window.maalrCallback = function(result) {
	console.log(JSON.stringify(result));
	$('.modal-body').empty();
	$('.modal-title').text("Übersetzung für '" + phrase + "'");

	if (result.data.length == 0) {
		$('.modal-body').append(
				"<p style='color:red;'>" + result.nothingFoundMessage + "</p>");
	}

	for ( var key in result.data) {
		if (result.data.hasOwnProperty(key)) {
			var a = result.data[key].a;
			var b = result.data[key].b;
			$('.modal-body').append('<p>' + a + ' | ' + b + '</p>');
		}
	}
	$('#myModal').modal('toggle');
}

$('.translate').click(
		function() {

			var id = $(this).attr("data-id");
			phrase = $(this).attr("data-word");
			var context = 'rumantschgrischun';
			var locale = 'rm';
			var maalr_max = 10;

			if (lang == "Sursilvan") {
				locale = "st"
			} else if (lang == "Surmiran") {
				context = "surmiran";
				locale = "sm"
			}

			var url = 'http://pledarigrond.ch/' + context
					+ '/json?pageNr=1&pageSize=' + maalr_max + '&locale='
					+ locale + '&values[searchPhrase]=' + phrase;

			console.log(url);

			$.ajax({
				type : 'GET',
				url : url,
				dataType : 'jsonp',
				async : false,
				cache : false,
				crossDomain : true,
				jsonpCallback : 'maalrCallback'
			});
		});

$('.translatable').click(function() {
	if (!locked) {

		locked = true;
		var lang_id = $(this).attr("data-lang-id");
		var url = "/drc/language?languageId=" + lang_id;

		$.ajax({
			type : 'GET',
			url : url,
			success : function(response) {
				lang = response;
			}
		});

		$(this).find('.word').addClass("selected");
		var wordWidth = $(this).find('.word').width() / 2;
		var margin = 175 - wordWidth;

		$(this).find('.translation').fadeIn(100);
		$(this).find('.translate').fadeIn(100);
		$(this).find('.arrow-down').fadeIn(100);
		$(this).find('.translation').css({
			'margin-left' : '-' + margin + 'px'
		});
		$(this).find('#word_lang').text('LANG: ' + lang);

		$(this).find('.closeB').click(function(event) {
			event.stopPropagation();
			locked = false;
			var parent = $(this).parent().parent();
			console.log(parent);
			$(parent).find(".translation").hide(100);
			$(parent).find(".translate").hide(100);
			$(parent).find(".arrow-down").hide(100);
			$(parent).find('.word').removeClass("selected");
		});
	}

});

$('.translatable').hover(function() {
	$(this).find('.word').css({
		'color' : '#15E9B1'
	});
}, function() {
	$(this).find('.word').css({
		'color' : '#707070'
	});
	$(this).find('.selected').css({
		'color' : '#15E9B1'
	});

});