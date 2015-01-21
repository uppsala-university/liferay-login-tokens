<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<liferay-theme:defineObjects />
<portlet:defineObjects />

<liferay-util:html-top outputKey="js_datatables">
		<script src='//ajax.aspnetcdn.com/ajax/jQuery/jquery-2.1.3.min.js'></script>
</liferay-util:html-top>

<h2>Login Tokens</h2>
Login tokens can be generated to be used from for instance mobile devices or as a one-time password. These tokens can easily be revoked later and done individually. So if you loose your smartphone or tablet you can revoke the token used by that specific device thus eliminating the need to reset a master password and change it across your entire ensamble of devices.
You're also eliminating the risk of someone decrypting your master password if you loose your computer or phone, as long as you revoke the token from within the portal.

<h2>Existing Tokens</h2>
<div class="existing-tokens">
    <table class="existing-tokens-table">
        <thead>
            <tr>
                <th>Description</th>
                <th>Created at</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach var="token" items="${tokens}" varStatus="status">
            <tr>
                <td class="token-description"><c:out value="${token.description}" /></td>
                <td class="token-created">${token.created}</td>
                <td><span class="btn btn-primary revoke-button" data-created="${token.created}">Revoke</span></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<h2>Generate new</h2>
<div class="generate-wrapper">
    To generate a new token, fill out a description below.
    <!--TODO: Before being able to generate one should authenticate again!
    TODO: Add copy to clipboard (without - ) on click!-->
    <div class="generate-form">
        <input type="text" class="description"/>
        <span class="btn btn-primary generate-token-button">Generate</span>
    </div>
</div>
<script>
<portlet:resourceURL id="GENERATE_NEW" var="generateTokenURL" />
<portlet:resourceURL id="REVOKE" var="revokeTokenURL" />
var jQ = jQuery.noConflict();

jQ(document).ready( function() {
    jQ( '.revoke-button' ).on('click', function() {
        var $this = jQ( this );
        jQ.ajax({
            type: "POST",
            url: "${revokeTokenURL}",
            data: {
                created: $this.data( 'created' )
            },
            success: function(data) {
                $this.parents('tr').fadeOut();
            }
        });
    });

    jQ( '.generate-token-button' ).on('click', function() {
        var $this = jQ( this );
        jQ.ajax({
            type: "POST",
            url: "${generateTokenURL}",
            data: {
                description: jQ( 'input.description').val()
            },
            success: function(data) {
                var jsonResp = jQuery.parseJSON( data );
                var parentContainer = $this.parents('.generate-wrapper');
                //Make the token visually readable
                var token = '';
                for( var i=0, len=jsonResp.token.length; i < len; i += 5 ) {
                   token += jsonResp.token.substr(i, 5) + (i >= (len-5) ? '' : ' - ');
                }

                parentContainer.hide();
                parentContainer.html( '<div class="generated-token-instruction">Copy this token to your application. It wont be shown again.</div><div class="generated-token"><span class="new-token" data-token="' + jsonResp.token + '">' + token + '</span></div>' );
                parentContainer.fadeIn();
            }
        });
    });
});
</script>