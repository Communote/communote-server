window.addEvent('domready', function() {
    init_tips();
});


/**
 * Initializes tips within the actual document.
 */
init_tips = function(parentContainer) {
    if (!window.communoteToolTips) {
        communoteToolTips = new AdvancedTips({ 
            hideDelay: null
        });
    }
    communoteToolTips.scan('a.tooltip', parentContainer);
};

/**
 * 
 * @param target
 * @return
 */
function goTo(target){

    if(typeOf(target) == 'number' && (target >= 0 || target < 6)){
        var form = $('command');
        
        if(form != null){
            var hiddenInput = new Element('input', {
                'type': 'hidden',
                'name': '_target'+target,
                'value': ''
                });
            hiddenInput.inject(form, 'top');
            // TODO does not work in IE :-(
            form.submit();
        }
    }
    
    return false;
}

/**
 * Function will submit the form if the event is based on a press of return
 * 
 * @return false if the form has been submitted
 */
function submitFormWithEnter(form, hiddenFieldId, value, e) {
    var keycode;
    if (window.event) {
        keycode = window.event.keyCode;
    } else if (e) {
        keycode = e.which;
    } else {
        return true;
    }
    if (keycode == 13) { // || keycode == 10 ?? mac ??
        var el = $(hiddenFieldId);
        el.setProperty('name', value);
        form.submit();
        return false;
    } else {
        return true;
    }
}

/**
 * enters the default port number for the selected database type.
 */
function setDefaultDatabasePortnumber(overwrite){
    var defaultPort, selectedElem;
    var dbType = $('databaseTypeIdentifier');
    var dbPort = $('databasePort');
    
    if(dbType == null || dbPort == null){
        return;
    }
    
    if(!overwrite && dbPort.get('value').length > 0){
        return;
    }
    selectedElem = dbType.getSelected();
    if (selectedElem != null && selectedElem.length == 1) {
        defaultPort = selectedElem[0].getProperty('data-cnt-database-default-port');
    }
    dbPort.set('value', defaultPort);
}

/**
 * 
 * @return
 */
function startDatabaseSetup(){
    var targetUrl = baseUrl+'installer/startDatabaseSetup.json';
    new Request.JSON({
        url: targetUrl,
        method: 'post',
        data: ''
    }).send();
}

/**
 * 
 * @return
 */
function observeDatabaseSetup(){
    var targetUrl = baseUrl+'installer/checkDatabaseSetupStatus.json';
    
    var jsonRequest = new Request.JSON({
        url: targetUrl,
        method: 'post',
        data: '',
        initialDelay: 3000,  // start after 3s
        delay: 5000,         // 5s delay between requests
        limit: 15000,        // 15s limit if no data is retrieved
        cancel: this.stopTimer,
        failure: this.stopTimer,
        exception: this.stopTimer
    });
    
    jsonRequest.addEvent('complete', function(jsonResponse) {

        if (jsonResponse != null) {
            var progress = jsonResponse.progress;
            var status = jsonResponse.status;
            var message = jsonResponse.message;
            
            // stop periodical request
            if(status == "FAILED"){
                this.stopTimer();
            }
                        
            // connecting to database
            if(progress == 0){
                handleStatusUpdate('connection', status, message);
            } else if(progress > 0) {
                handleStatusUpdate('connection', 'SUCCEEDED', '');
            }
            
            // preparing installation
            if(progress == 1){
                handleStatusUpdate('preparing', status, message);
            } else if(progress > 1) {
                handleStatusUpdate('preparing', 'SUCCEEDED', '');
            }

            // create database schema
            if(progress == 2){
                handleStatusUpdate('schema', status, message);
            } else if(progress > 2) {
                handleStatusUpdate('schema', 'SUCCEEDED', '');
            }
            
            // writing initial data
            if(progress == 3){
                
                handleStatusUpdate('data', status, message);
                
                if(status == 'SUCCEEDED'){
                    // stop periodical request
                    this.stopTimer();

                    var reports = $('reports');
                    if(reports != null && message != null){
                        showLoading(false);
                        showReport(true, 'success-report', message);
                    }
                    
                    // show next button
                    $$('div.button-right')[0].removeClass('hidden');
                }
            }
            
        } else {
            // stop periodical request
            this.stopTimer();
            return false;
        }
    }.bind(jsonRequest));
    
    jsonRequest.startTimer();
}

/**
 * 
 * @param selector
 * @param status
 * @param message
 * @return
 */
function handleStatusUpdate(selector, status, message){
    var element = $(selector);
    
    if(element == null){
        return false;
    }
    var col1 = element.getElements('div')[0];
    var col2 = element.getElements('div')[1];
    
    if(status == "STARTED"){
        
        col1.addClass('active');
        col2.empty(); 
        col2.grab(getStatusIcon(status)); 
        
    } else if(status == "SUCCEEDED"){
        
        col1.removeClass('active');
        col2.empty(); 
        col2.grab(getStatusIcon(status)); 
        
    } else if (status == "FAILED"){
        
        col1.removeClass('active');
        col2.empty(); 
        col2.grab(getStatusIcon(status)); 
        
        var reports = $('reports');
        if(reports != null && message != null){
            showLoading(false);
            showReport(true, 'warning-report', message);
        }
    }    
}

/**
 * 
 * @param status
 * @return
 */
function getStatusIcon(status){
    var icon = new Element('span');
    
    if(status == 'STARTED'){
        icon = new Element('img', {
            'src': contextUrl+'themes/core/images/misc/loading-small.gif',
            'class': 'loading',
            'alt': 'loading',
            'border': '0'                
        });
    }else if (status == 'SUCCEEDED'){
        icon = new Element('img', {
            'src': contextUrl+'themes/core/images/misc/database_setup_success.png',
            'alt': 'success',
            'border': '0'                
        });
    } else if (status == 'FAILED'){
        icon = new Element('img', {
            'src': contextUrl+'themes/core/images/misc/database_setup_error.png',
            'alt': 'error',
            'border': '0'                
        });
    }
    
    return icon;
}

/**
 * 
 * @return
 */
function startApplicationInitialization(targetUrl){
    
    var jsonRequest = new Request.JSON({
        url: targetUrl,
        method: 'post'
    });
        
    jsonRequest.send();
}

/**
 * 
 * @return
 */
function observeApplicationInitialization(targetUrl){
    
    var jsonRequest = new Request.JSON({
        url: targetUrl,
        method: 'post',
        data: '',
        initialDelay: 3000,  // start after 3s
        delay: 5000,         // 5s delay between requests
        limit: 15000,        // 15s limit if no data is retrieved
        cancel: this.stopTimer,
        failure: this.stopTimer,
        exception: this.stopTimer
    });
    
    jsonRequest.addEvent('complete', function(jsonResponse) {
        
        var status = jsonResponse.status;
        
        var reports = $('reports');
        if(reports == null){
            return false;
        }
        
        if (status == "ERROR"){
            showLoading(false);
            showReport(true, 'warning-report', null);
         
            // stop periodical request
            this.stopTimer();
        } else if(status == "OK") {
            var div = $('final-step');
            div.empty();

            showLoading(false);
            showReport(true, 'success-report', null);
            
            // show homepage button
            $$('div.button-right')[0].removeClass('hidden');
            
            // stop periodical request
            this.stopTimer();
        }
    }.bind(jsonRequest));
    
    jsonRequest.startTimer();
}

/**
 * 
 * @return
 */
function sendTestMessage(){
    var targetUrl = baseUrl+'installer/sendTestMessage.json';

    var fields = $('command').getElements('input[type=text],input[type=password],input[type=checkbox]');
    var postData = {};
    
    for( var i = 0; i < fields.length; i++){
        var field = fields[i];
        if(field.get('type') == 'checkbox'){
            postData[field.get('id')] = field.checked;
        }else {
            postData[field.get('id')] = field.get('value');
        }
    }
    
    var jsonRequest = new Request.JSON({
        url: targetUrl,
        method: 'post',
        data: postData
    });
    
    jsonRequest.addEvent('complete', function(jsonResponse) {
        var status = jsonResponse.status;
        var message = jsonResponse.message;

        var reports = $('reports');
        if(reports == null && message == null){
            return false;
        }
        
        if (status == "ERROR"){
            showLoading(false);
            showReport(true, 'warning-report', message);
        } else {
            showLoading(false);
            showReport(true, 'success-report', message);
        }
    });

    jsonRequest.addEvent('failure', function(jsonResponse) {
        showReports(false);
    });
    
    showLoading(true);
    
    jsonRequest.send();
}

/**
 * 
 * @param show
 * @return
 */
function showLoading(show){
	var elements, i, loading;
    var reports = $('reports');

    if(reports == null){
        return false;
    }
    
    // hide all other elements
    elements = reports.getElements('div[class*=-report]');
    for (i=0;i<elements.length;i++){
        elements[i].addClass('hidden');
    }
     
    showReports(show); 
    loading = reports.getElement('div.loading');
    if(show){
        loading.removeClass('hidden');
    } else {
        loading.addClass('hidden');
    }   
    
    return true;
}

/**
 * 
 * @param show
 *            true if the reports container should
 * @return
 */
function showReport(show, selector, message){
    var reports = $('reports');

    if(reports == null){
        return false;
    }
    
    var report = reports.getElement('div.'+selector);
    
    if(report == null){
        return false;
    }
    
    // switch message text
    if(message != null){
        var msg = report.getElement('div.message');
        msg.empty();
        msg.set('html', message);
    }
    
    // show/hide the defined report
    if(show){
        report.removeClass('hidden');
    } else {
        report.addClass('hidden');
    }        
    
    // show/hide the main area for reports
    showReports(show);

    return true;
}

/**
 * 
 * @param show
 *            true if the reports container should
 * @return
 */
function showReports(show){
    var reports = $('reports');

    if(reports == null){
        return false;
    }
    
    if(show){
        reports.removeClass('hidden');
    } else {
        reports.addClass('hidden');
    }        
    
    return true;
}

function setLocationSettings(){
    var form = $('command');
    
    if(form != null){
        var webHost = new Element('input', {
            'type': 'hidden',
            'name': 'webHost',
            'value': window.location.hostname
            });
        webHost.inject(form, 'top');

        var webPort = new Element('input', {
            'type': 'hidden',
            'name': 'webPort',
            'value': window.location.port
            });
        webPort.inject(form, 'top');
        
        var webProtocol = new Element('input', {
            'type': 'hidden',
            'name': 'webProtocol',
            'value': window.location.protocol
            });
        webProtocol.inject(form, 'top');
    }
}
/*
 * ---
 * 
 * script: Request.Periodical.js
 * 
 * description: Requests the same URL to pull data from a server but increases the intervals if no
 * data is returned to reduce the load
 * 
 * license: MIT-style license
 * 
 * authors: - Christoph Pojer
 * 
 * requires: - core:1.3/Request - /MooTools.More
 * 
 * provides: [Request.Periodical]
 * 
 * ...
 */
Request.implement({

    options: {
        initialDelay: 5000,
        delay: 5000,
        limit: 60000
    },

    startTimer: function(data){
        var fn = function(){
            if (!this.running) this.send({data: data});
        };
        this.timer = fn.delay(this.options.initialDelay, this);
        this.lastDelay = this.options.initialDelay;
        this.completeCheck = function(response){
            clearTimeout(this.timer);
            this.lastDelay = (response) ? this.options.delay : (this.lastDelay + this.options.delay).min(this.options.limit);
            this.timer = fn.delay(this.lastDelay, this);
        };
        return this.addEvent('complete', this.completeCheck);
    },

    stopTimer: function(){
        clearTimeout(this.timer);
        return this.removeEvent('complete', this.completeCheck);
    }

});