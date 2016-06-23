<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<script type="text/javascript">
 function btn_click(){
	  var fileNo=Number(document.getElementById('fileCount').value)+1;
	  newDiv=document.createElement("div")
	  newDiv.id="divFile"+fileNo;
	  document.getElementById('uploadDiv').appendChild(newDiv)
	  document.getElementById('fileCount').value=fileNo;
	  newDiv.innerHTML="<input type='file' name='file"+fileNo+"'>";
 }
</script>

<form method="post" action="./fileupload.do" enctype="multipart/form-data">
	<div id="uploadDiv">
    	<input type="file" name="file1" />
    </div>
            <input type="button" value="Load" onclick="javascript:btn_click();">
            <input type="hidden" id="fileCount" name="fileCount" value="1">
            <input type="submit" value="Save" />
 </form>