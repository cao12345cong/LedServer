$(document).ready (function () {
	$("#btnDetect").click(function(){
		$.ajax({
			cache:false,
			type:"POST",
			url:"/sendercard",
			data:{
				detectCard:"detectCard"
			},
			async:true
			
		});
	
	})						 
							 
	$("#res_w_h").hide();					 
    $("#btn_res_set").hide();
	
	$("#resolutions").change(function(){
		var first=$(this).children('option:first').html();//
		var selectvalue=$(this).children('option:selected').html();//
		if(selectvalue==first){
			$("#res_w_h").show();					 
    		$("#btn_res_set").show();	
			return;
		}else{
			$("#res_w_h").hide();					 
    		$("#btn_res_set").hide();
		}
		
		$.ajax({
			cache:false,
			type:"POST",
			url:"/sendercard",
			data:{
				resolution:selectvalue
			},
			async:true
		});
	
	})
	
	
	
	$("#btn_res_set").click(function(){
		
		var width=$("#input_res_w").val();	
		
		var height=$("#input_res_h").val();
		var frameRate=$("#frame_rate").children('option:selected').html()
		
		
		if(width==null||width==''){
			return;	
		}
		if(height==null||height==''){
			return;	
		}
		$.ajax({
			cache:false,
			type:"POST",
			url:"/sendercard",
			data:{
				resolutionWidth:width,
				resolutionHeight:height,
				resolutionFrameRate:frameRate
			},
			async:true
		});
	})
	
	
	$("#videoSource").change(function(){
		
		var videoSource=$(this).children('option:selected').val();//
		$.ajax({
			cache:false,
			type:"POST",
			url:"/sendercard",
			data:{
				videoSource:videoSource
			},
			async:true
		});
	
	})
})							