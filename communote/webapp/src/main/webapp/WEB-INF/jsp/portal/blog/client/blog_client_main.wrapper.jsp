<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<tiles:importAttribute/>

<div class="panel">
<h4>${innerTitle} &gt; <span>${innerSubtitle}</span></h4>
</div>
<div class="wrapper last">
    <tiles:insert attribute="innerContent" />
</div>