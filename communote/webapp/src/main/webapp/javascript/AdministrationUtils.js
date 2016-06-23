/**
 * Adds a new container with input fields to the LDAP configuration.
 * 
 * @param type type can be 'user' or 'usergroup'
 */
function addSearchBaseFields(type) {
    var content = $(type + '-searchbase-INDEX').get('html');
    // var nextIndex = $$('div.' + type + '-searchbase').length;
    var lastElmId = $$('div.' + type + '-searchbase').getLast().get('id');
    var nextIndex = Number(lastElmId.split('-').getLast()) + 1;

    content = content.replace(/DUMMY-/g, '');
    content = content.replace(/INDEX/g, nextIndex);

    var newContainer = new Element('div', {
        'id': type + '-searchbase-' + nextIndex,
        'class': type + '-searchbase',
        'html': content
    });

    newContainer.inject($$('div.' + type + '-searchbase').getLast(), 'after');
}

/**
 * Empties an Element of all its children, removes and garbages the Element.
 */
function removeContainer(id) {
    var container = $(id);

    if (container != null) {
        container.destroy();
    }
}

function toggleVisibility(id, isVisible) {
    if (isVisible) {
        $(id).removeClass('hidden');
    } else {
        $(id).addClass('hidden');
    }
}

/**
 * @param id Id of the field to enable.
 * @param id Id of the field to note, that the password was changed.
 * @param caller The element, which invoked this method.
 */
function enableField(id, idOfPasswordChangedField, caller) {
    $(id).removeProperty('disabled');
    $(idOfPasswordChangedField).set('value', 'true');
    if (caller) {
        caller.dispose();
    }
}