function getExerciseBlocksContainer() {
    return document.getElementById('exerciseBlocks');
}

function getExerciseTemplateHtml() {
    const template = document.getElementById('exercise-template');
    const optionsTemplate = document.getElementById('exercise-options-template');

    if (!template || !optionsTemplate) {
        return null;
    }

    return {
        exerciseTemplateHtml: template.innerHTML.trim(),
        optionsHtml: optionsTemplate.innerHTML.trim(),
        setTemplateHtml: document.getElementById('set-template').innerHTML.trim()
    };
}

function createSetRowHtml(exerciseIndex, setIndex) {
    const templates = getExerciseTemplateHtml();
    if (!templates) {
        return '';
    }

    return templates.setTemplateHtml
        .replaceAll('__EXERCISE_INDEX__', exerciseIndex)
        .replaceAll('__SET_INDEX__', setIndex)
        .replaceAll('__SET_NUMBER__', setIndex + 1);
}

function addExerciseBlock() {
    const container = getExerciseBlocksContainer();
    const templates = getExerciseTemplateHtml();

    if (!container || !templates) {
        return;
    }

    const exerciseIndex = container.querySelectorAll('.exercise-card').length;
    const html = templates.exerciseTemplateHtml
        .replaceAll('__EXERCISE_INDEX__', exerciseIndex)
        .replaceAll('__EXERCISE_NUMBER__', exerciseIndex + 1)
        .replaceAll('__EXERCISE_OPTIONS__', templates.optionsHtml)
        .replaceAll('__SET_ROWS__', createSetRowHtml(exerciseIndex, 0));

    const wrapper = document.createElement('div');
    wrapper.innerHTML = html;
    container.appendChild(wrapper.firstElementChild);
}

function addSetRow(exerciseIndex) {
    const setsContainer = document.getElementById('exercise-' + exerciseIndex + '-sets');
    const templates = getExerciseTemplateHtml();

    if (!setsContainer || !templates) {
        return;
    }

    const setIndex = setsContainer.querySelectorAll('.set-row').length;
    const html = templates.setTemplateHtml
        .replaceAll('__EXERCISE_INDEX__', exerciseIndex)
        .replaceAll('__SET_INDEX__', setIndex)
        .replaceAll('__SET_NUMBER__', setIndex + 1);

    const wrapper = document.createElement('div');
    wrapper.innerHTML = html;
    setsContainer.appendChild(wrapper.firstElementChild);
}

window.addExerciseBlock = addExerciseBlock;
window.addSetRow = addSetRow;

document.addEventListener('DOMContentLoaded', function () {
    const container = getExerciseBlocksContainer();

    if (container && container.querySelectorAll('.exercise-card').length === 0) {
        addExerciseBlock();
    }
});

