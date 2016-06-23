(function(namespace, window) {
    var classNamespace = ((namespace && namespace.classes) || window);

    classNamespace.TopicWriteAccessEvaluator = function(topicUtils) {
        this.topicUtils = topicUtils;
        this.runBeforeEvaluators = undefined;
    };
    
    classNamespace.TopicWriteAccessEvaluator.prototype.addEvaluationFunction = function(evaluationFunction) {
        if (evaluationFunction) {
            if (!this.runBeforeEvaluators) {
                this.runBeforeEvaluators = [];
            }
            this.runBeforeEvaluators.push(evaluationFunction);
        }
    };
    
    classNamespace.TopicWriteAccessEvaluator.prototype.checkWriteAccess = function(topicId, initialTargetTopic) {
        var i, l, result;
        if (this.runBeforeEvaluators) {
            for (i = 0, l = this.runBeforeEvaluators.length; i < l; i++) {
                result = this.runBeforeEvaluators[i].call(null, topicId, initialTargetTopic);
                if (result && result.writeAccess === false && result.subtopicWriteAccess === false) {
                    return result;
                }
            }
        }
        if (!result || result.writeAccess == undefined) {
            result = {
                writeAccess: true
            };
        }
        if (topicId != null) {
            if (initialTargetTopic && topicId == initialTargetTopic.id) {
                if (initialTargetTopic.notFound) {
                    // if the topic to check is the initial target topic and we know that it does
                    // not exist, we can stop here
                    return {
                        writeAccess: false,
                        subtopicWriteAccess: false,
                        message: initialTargetTopic.notFoundErrorMessage
                    };
                }
                // skip one request if already know that the topic is not readable
                if (initialTargetTopic.noReadAccess) {
                    result.writeAccess = false;
                }
            }
            // check actual write access via topic tools if writeAccess is still true
            if (result.writeAccess == true) {
                // if the role for a topic is not known (e.g. because of an error when
                // communicating with the server), be optimistic and show the editor, since an
                // error message will pop up if the user can't write
                result.writeAccess = this.topicUtils.blogWriteAccess(topicId, true);
            }
            if (!result.writeAccess) {
                // the user has no access to the topic, but maybe to the subtopics?
                result.subtopicWriteAccess = this.topicUtils.getWritableSubtopics(topicId).length > 0;
            }
        } 
        return result;
    };
    
    classNamespace.TopicWriteAccessEvaluator.prototype.removeEvaluationFunction = function(evaluationFunction) {
        var idx;
        if (evaluationFunction) {
            if (this.runBeforeEvaluators) {
                idx = this.runBeforeEvaluators.indexOf(evaluationFunction);
                if (idx >= 0) {
                    this.runBeforeEvaluators.splice(idx, 1);
                }
            }
        }
    };
    
})(this.runtimeNamespace, this);