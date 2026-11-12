'use strict';

(function () {
    const input = document.getElementById('searchQuery');
    const tbody = document.getElementById('booksTableContent');
    const errorMessage = document.getElementById('booksearchErrorMessage');

    function cell(text) {
        const td = document.createElement('td');
        // textContent never interprets HTML, so book data cannot inject markup or scripts
        td.textContent = text;
        return td;
    }

    function addRow(book) {
        const row = document.createElement('tr');
        row.appendChild(cell(book.id));
        row.appendChild(cell(book.name));
        row.appendChild(cell(book.author));
        row.appendChild(cell(Number(book.price).toFixed(2)));

        const link = document.createElement('a');
        link.href = '/books?id=' + encodeURIComponent(book.id);
        link.textContent = 'Details';
        const linkCell = document.createElement('td');
        linkCell.appendChild(link);
        row.appendChild(linkCell);

        tbody.appendChild(row);
    }

    function showSearchTerm(query) {
        document.getElementById('searchTerm').textContent = query;
        document.getElementById('searchSummary').classList.remove('invisible');
    }

    function search() {
        const query = input.value;
        tbody.replaceChildren();
        errorMessage.classList.add('invisible');
        showSearchTerm(query);

        secureFetch('/api/books/search?query=' + encodeURIComponent(query))
            .then(function (response) {
                if (!response.ok) {
                    throw new Error(response.statusText);
                }
                return response.json();
            })
            .then(function (books) {
                books.forEach(addRow);
            })
            .catch(function () {
                errorMessage.classList.remove('invisible');
            });
    }

    document.getElementById('booksearchButton').addEventListener('click', search);
    input.addEventListener('keyup', function (event) {
        if (event.key === 'Enter') {
            search();
        }
    });
})();
