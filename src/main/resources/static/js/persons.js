'use strict';

(function () {
    const input = document.getElementById('searchInput');
    const tableContent = document.getElementById('tableContent');

    function cell(text) {
        const td = document.createElement('td');
        // Names and e-mails are user controlled: never assign them through innerHTML
        td.textContent = text;
        return td;
    }

    function addRow(person) {
        const row = document.createElement('tr');
        row.appendChild(cell(person.id));
        row.appendChild(cell(person.firstName));
        row.appendChild(cell(person.lastName));
        row.appendChild(cell(person.email));

        const link = document.createElement('a');
        link.href = '/persons/' + encodeURIComponent(person.id);
        link.textContent = 'View profile';
        const linkCell = document.createElement('td');
        linkCell.appendChild(link);
        row.appendChild(linkCell);

        tableContent.appendChild(row);
    }

    function search() {
        const searchTerm = input.value;
        secureFetch('/persons/search?searchTerm=' + encodeURIComponent(searchTerm))
            .then(function (response) {
                return response.ok ? response.json() : [];
            })
            .then(function (persons) {
                tableContent.replaceChildren();
                persons.forEach(addRow);
                document.getElementById('searchTerm').textContent = searchTerm;
                document.getElementById('searchContainer').classList.remove('invisible');
            });
    }

    document.getElementById('searchButton').addEventListener('click', search);
    input.addEventListener('keyup', function (event) {
        if (event.key === 'Enter') {
            search();
        }
    });
})();
