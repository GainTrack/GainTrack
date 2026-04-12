function getExerciseBlocksContainer() {
    return document.getElementById('exerciseBlocks');
}

function getOrCreateExerciseErrorElement(exerciseCard) {
    let errorElement = exerciseCard.querySelector('[data-role="exercise-error"]');
    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.className = 'workout-inline-error d-none';
        errorElement.setAttribute('data-role', 'exercise-error');
        const cardBody = exerciseCard.querySelector('.card-body');
        if (cardBody) {
            cardBody.insertBefore(errorElement, cardBody.children[2] || null);
        }
    }

    return errorElement;
}

function showExerciseError(exerciseCard, message) {
    const errorElement = getOrCreateExerciseErrorElement(exerciseCard);
    if (!errorElement) {
        return;
    }

    errorElement.textContent = message;
    errorElement.classList.remove('d-none');
    exerciseCard.classList.add('workout-exercise-card-error');
}

function clearExerciseError(exerciseCard) {
    const errorElement = exerciseCard.querySelector('[data-role="exercise-error"]');
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.classList.add('d-none');
    }
    exerciseCard.classList.remove('workout-exercise-card-error');
}

async function fetchPartial(url) {
    const response = await fetch(url, {headers: {'X-Requested-With': 'XMLHttpRequest'}});
    if (!response.ok) {
        throw new Error('Failed to load form partial.');
    }

    return response.text();
}

function updateAttributesForExercise(element, exerciseIndex) {
    if (element.name) {
        element.name = element.name.replace(/exercises\[\d+]/g, 'exercises[' + exerciseIndex + ']');
    }

    if (element.id) {
        element.id = element.id.replace(/exercise-\d+/g, 'exercise-' + exerciseIndex);
    }

    if (element.htmlFor) {
        element.htmlFor = element.htmlFor.replace(/exercise-\d+/g, 'exercise-' + exerciseIndex);
    }
}

function updateAttributesForSet(element, exerciseIndex, setIndex) {
    if (element.name) {
        element.name = element.name
            .replace(/exercises\[\d+]/g, 'exercises[' + exerciseIndex + ']')
            .replace(/sets\[\d+]/g, 'sets[' + setIndex + ']');
    }

    if (element.id) {
        element.id = element.id
            .replace(/exercise-\d+/g, 'exercise-' + exerciseIndex)
            .replace(/set-\d+/g, 'set-' + setIndex);
    }

    if (element.htmlFor) {
        element.htmlFor = element.htmlFor
            .replace(/exercise-\d+/g, 'exercise-' + exerciseIndex)
            .replace(/set-\d+/g, 'set-' + setIndex);
    }
}

function reindexSetRows(exerciseCard, exerciseIndex) {
    const setRows = exerciseCard.querySelectorAll('.set-row');

    setRows.forEach(function (setRow, setIndex) {
        setRow.dataset.setIndex = String(setIndex);
        setRow.querySelectorAll('[name], [id], label[for]').forEach(function (element) {
            updateAttributesForSet(element, exerciseIndex, setIndex);
        });

        const setNumberInput = setRow.querySelector('.set-number');
        if (setNumberInput) {
            setNumberInput.value = String(setIndex + 1);
        }
    });
}

function reindexExerciseCards() {
    const container = getExerciseBlocksContainer();
    if (!container) {
        return;
    }

    const exerciseCards = container.querySelectorAll('.exercise-card');
    exerciseCards.forEach(function (exerciseCard, exerciseIndex) {
        exerciseCard.dataset.exerciseIndex = String(exerciseIndex);

        const title = exerciseCard.querySelector('.exercise-title');
        if (title) {
            title.textContent = 'Exercise ' + (exerciseIndex + 1);
        }

        exerciseCard.querySelectorAll('[name], [id], label[for]').forEach(function (element) {
            updateAttributesForExercise(element, exerciseIndex);
        });

        const setsContainer = exerciseCard.querySelector('.set-rows');
        if (setsContainer) {
            setsContainer.id = 'exercise-' + exerciseIndex + '-sets';
        }

        reindexSetRows(exerciseCard, exerciseIndex);
    });
}

async function addExerciseBlock() {
    const container = getExerciseBlocksContainer();
    if (!container) {
        return;
    }

    const exerciseIndex = container.querySelectorAll('.exercise-card').length;

    try {
        const html = await fetchPartial('/workouts/form/exercise-row?exerciseIndex=' + exerciseIndex);
        container.insertAdjacentHTML('beforeend', html);
        reindexExerciseCards();
    } catch (error) {
        console.error(error);
    }
}

async function addSetRow(exerciseIndex) {
    const setsContainer = document.getElementById('exercise-' + exerciseIndex + '-sets');
    if (!setsContainer) {
        return;
    }

    const setIndex = setsContainer.querySelectorAll('.set-row').length;

    try {
        const html = await fetchPartial('/workouts/form/set-row?exerciseIndex=' + exerciseIndex + '&setIndex=' + setIndex);
        setsContainer.insertAdjacentHTML('beforeend', html);

        const exerciseCard = setsContainer.closest('.exercise-card');
        if (exerciseCard) {
            reindexSetRows(exerciseCard, exerciseIndex);
        }
    } catch (error) {
        console.error(error);
    }
}

function removeExercise(button) {
    const container = getExerciseBlocksContainer();
    const exerciseCard = button.closest('.exercise-card');
    if (!exerciseCard || !container) {
        return;
    }

    if (container.querySelectorAll('.exercise-card').length <= 1) {
        showExerciseError(exerciseCard, 'Workout has to have at least 1 exercise!');
        return;
    }

    exerciseCard.remove();
    reindexExerciseCards();
}

function removeSet(button) {
    const setRow = button.closest('.set-row');
    const exerciseCard = button.closest('.exercise-card');
    if (!setRow || !exerciseCard) {
        return;
    }

    const setRows = exerciseCard.querySelectorAll('.set-row');
    if (setRows.length <= 1) {
        showExerciseError(exerciseCard, 'Each exercise has to have at least 1 set!');
        setRow.classList.add('workout-set-row-error');
        return;
    }

    clearExerciseError(exerciseCard);

    setRow.remove();

    const exerciseIndex = Number(exerciseCard.dataset.exerciseIndex);
    reindexSetRows(exerciseCard, exerciseIndex);
}

window.addExerciseBlock = addExerciseBlock;
window.addSetRow = addSetRow;

document.addEventListener('DOMContentLoaded', function () {
    const container = getExerciseBlocksContainer();
    if (!container) {
        return;
    }

    reindexExerciseCards();

    document.addEventListener('click', function (event) {
        const target = event.target;
        if (!(target instanceof Element)) {
            return;
        }

        const actionButton = target.closest('[data-action]');
        if (!actionButton) {
            return;
        }

        const action = actionButton.getAttribute('data-action');
        if (action === 'add-exercise') {
            addExerciseBlock();
        }
        if (action === 'add-set') {
            const exerciseCard = actionButton.closest('.exercise-card');
            if (exerciseCard) {
                clearExerciseError(exerciseCard);
                exerciseCard.querySelectorAll('.set-row').forEach(function (row) {
                    row.classList.remove('workout-set-row-error');
                });
                addSetRow(Number(exerciseCard.dataset.exerciseIndex));
            }
        }
        if (action === 'remove-exercise') {
            removeExercise(actionButton);
        }
        if (action === 'remove-set') {
            removeSet(actionButton);
        }
    });

    if (container.querySelectorAll('.exercise-card').length === 0) {
        addExerciseBlock();
    }
});

