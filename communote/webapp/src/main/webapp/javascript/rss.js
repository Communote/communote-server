function addRSSFeedToHeader(){

}

function updateRSSList(){
	
	var root = document.childNodes[0];
	var links = document.childNodes[0].childNodes[0].childNodes;
	var header = document.childNodes[0].childNodes[0];
	
	for (var i = 0; i < links.length; ++i){
		if(links[i].nodeType==1){
			if(links[i].getAttribute('name')=='RssHeaderLink') {
				header.removeChild(links[i]);
			}
		}
	}
	var widgetRssLink = document.getElementsByName('rssLink');
	
	for(var j=0; j < widgetRssLink.length; j++) {
		var link = document.createElement("link");
		link.setAttribute("rel", "alternate");
		link.setAttribute("type", "application/rss+xml");
		var path = widgetRssLink[j].getAttribute('href');
		link.setAttribute("href", path);
		header.appendChild(link);
	}
	
	header = root.removeChild(root.firstChild);
	root.appendChild(header);
	
}