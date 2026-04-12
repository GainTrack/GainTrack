let deleteModalInstance;

function openDeleteModal(button) {
    const id = button.getAttribute('data-id');
    const name = button.getAttribute('data-name');

    const modal = document.getElementById('deleteModal');
    const form = document.getElementById('deleteForm');
    const text = document.getElementById('deleteModalText');

    form.action = '/exercises/' + id + '/delete';
    text.textContent = 'Are you sure you want to delete the exercise "' + name + '"?';

    if (window.bootstrap) {
        deleteModalInstance = bootstrap.Modal.getOrCreateInstance(modal);
        deleteModalInstance.show();
    }
}

