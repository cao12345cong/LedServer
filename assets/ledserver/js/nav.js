$(document).ready (function () {
	String.prototype.startWith=function(s){
	  if(s==null||s==""||this.length==0||s.length>this.length)
	     return false;
	  if(this.substr(0,s.length)==s)
		 return true;
	  else
		 return false;
	  return true;
	 }
						 
	String.prototype.endWith=function(s){
	  if(s==null||s==""||this.length==0||s.length>this.length)
		 return false;
	  if(this.substring(this.length-s.length)==s)
		 return true;
	  else
		 return false;
	  return true;
	 }						 
							 
							 
							 
	var uri = window.location.href;
	var flag=uri.endWith("html");
	if(uri.endWith("index")||uri.endWith("/")||uri.endWith("8080")){
		
		$("#index a").css("font-weight","bold");	
		$("#index a").css("background","#df5538");
	}else if(uri.endWith("screenmanager")){
		$("#screenmanager a").css("font-weight","bold");
		$("#screenmanager a").css("background","#df5538");
		
	}else if(uri.endWith("receivercard")){
		$("#receivercard a").css("font-weight","bold");
		$("#receivercard a").css("background","#df5538");
	}else if(uri.endWith("program")){
		$("#program a").css("font-weight","bold");
		$("#program a").css("background","#df5538");
	}else if(uri.endWith("setting")){
		$("#setting a").css("font-weight","bold");
		$("#setting a").css("background","#df5538");
	}
	

})