var name;

function start()
{
if ($.cookie("name"))
{
	name = $.cookie("name");
} 
else
{
	name = window.prompt( "Please enter your name");
	$.cookie("name", name);
}
	output();
}

function getGreeting()
{
	var now = new Date(); 
	var hour = now.getHours();
	if ( hour < 12 )
	return "Good Morning, ";
	else
	{
		hour = hour - 12; 
		if ( hour < 6 )
			return "Good Afternoon, ";
 		else
			return "Good Evening, ";
	}
}

function output()
{
	var greeting = getGreeting();
	document.getElementById("welcome").innerHTML = "<p>"+ greeting + name + ". Welcome to Assignment 3!</p>";
	document.getElementById("url").innerHTML = "<p><a href = 'javascript:wrongPerson()'> " +
	"Click here if you are not " + name + ".</a></p>";
}

function wrongPerson()
{
	$.cookie("name", null, {expires: -1});

	location.reload();
} 

window.addEventListener("load",start,false);