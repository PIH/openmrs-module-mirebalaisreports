<p id="${ config.id }">
    <label for="${ config.id }-field">${ config.label }</label>
    <select id="${ config.id }-field" name="${ config.formFieldName }">
        <% months.each { %>
        <option value="${ it.value }">${ it.label }</option>
        <% } %>
    </select>
</p>