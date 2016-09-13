$(document).ready (function () {
	
	$("#video ul li").click(function(){
		//alert(1);
		var v=$(this).children("span").html();
		var len=v.length;
		var index;
		if(len<=3){
			index=0;
		}else{
			index=v.substring(3);
		}
		
		$("ul li").css("background","");
		$(this).css("background","#cccccc");
//		$.post("/program",
//		{
//		  setProgramIndex:index
//		 
//		},
//		null);
		$.ajax({
			cache:false,
			type:"POST",
			url:"/program",
			data:{
				setProgramIndex:index
			},
			async:true
		});

	})
	
	$("#language").change(function(){ 						 
		var value=$(this).children('option:selected').val();// 

		$.ajax({
			cache:false,
			type:"POST",
			url:"/setting",
			data:{
				language:value
			},
			async:true
		});
		//window.location.href="index?onoff="+value+"";		 
	})				
							 
})