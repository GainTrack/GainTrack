function openDeleteModal(button) {
    const id = button.getAttribute('data-id');
    const name = button.getAttribute('data-name');

    const modal = document.getElementById('deleteModal');
    const form = document.getElementById('deleteForm');
    const text = document.getElementById('deleteModalText');

    form.action = '/exercises/' + id + '/delete';
    text.textContent = 'Jeste li sigurni da želite izbrisati vježbu "' + name + '"?';

    modal.classList.add('show');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.remove('show');
}

window.addEventListener('click', function (event) {
    const modal = document.getElementById('deleteModal');
    if (event.target === modal) {
        closeDeleteModal();
    }
});