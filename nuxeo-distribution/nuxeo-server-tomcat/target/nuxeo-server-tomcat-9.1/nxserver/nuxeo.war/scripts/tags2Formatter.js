var nuxeo = nuxeo || {};

function createNewTag(term, data) {
    return {
        id : term,
        displayLabel : term,
        newTag : true
    };
}

function addTagHandler(tag) {
    return addTagging(tag.id);
}

function removeTagHandler(tag) {
    return removeTagging(tag.id);
}

function formatSuggestedTags(tag) {
    var escapeHTML = nuxeo.utils.escapeHTML;
    if (tag.newTag) {
        return "<span class='s2newTag'>" + escapeHTML(tag.displayLabel) + "</span>"
    } else {
        return "<span class='s2existingTag'>" + escapeHTML(tag.displayLabel) + "</span>"
    }
}

function formatSelectedTags(tag) {
    var escapeHTML = nuxeo.utils.escapeHTML;
    var jsFragment = "listDocumentsForTag('" + escapeHTML(tag.displayLabel) + "');";
    return '<span class="s2newTag"><a href="' + window.nxContextPath + '/search/tag_search_results.faces?conversationId=' + currentConversationId + '" onclick="' + jsFragment + '">'
            + escapeHTML(tag.displayLabel) + '</a></span>'
}
