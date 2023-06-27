package service;

import model.Epic;
import model.States;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final Map <Integer, Task> tasks = new HashMap<>();
    private final Map <Integer, Epic> epics = new HashMap<>();
    private final Map <Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();

    public HistoryManager getInMemoryHistoryManager() {
        return inMemoryHistoryManager;
    }

    private int getNextId() {
        return nextId++;
    }

    @Override
    public List<Task> getListTasks () {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks () {
        for (Task task : tasks.values()) {
            inMemoryHistoryManager.remove(task.getId());
        }

        tasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        inMemoryHistoryManager.add(task);
        return task;
    }

    @Override
    public void createTask(Task task) {
        if (task != null) {
            task.setId(getNextId());
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsValue(task)) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            inMemoryHistoryManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public List<Epic> getListEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            inMemoryHistoryManager.remove(epic.getId());
        }

        for (Subtask subtask : subtasks.values()) {
            inMemoryHistoryManager.remove(subtask.getId());
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        inMemoryHistoryManager.add(epic);
        return epic;
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic != null) {
            epic.setId(getNextId());
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null && epics.containsValue(epic)) {
            final Epic savedEpic = epics.get(epic.getId());
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (Integer subtask : epic.getSubtasks()) {
                inMemoryHistoryManager.remove(subtask);
                subtasks.remove(subtask);
            }

            inMemoryHistoryManager.remove(id);
            epics.remove(id);
        }
    }

    private void updateStateEpic(Epic epic){
        if (epic != null && epics.containsValue(epic)) {

            List<Integer> subs = epic.getSubtasks();

            if (subs.isEmpty()) {
                epic.setState(States.NEW);
                return;
            }

            States status = null;

            for (int id : subs) {
                final Subtask subtask = subtasks.get(id);

                if (subtask == null) {
                    continue;
                }

                if (status == null) {
                    status = subtask.getState();
                    continue;
                }

                if (status.equals(subtask.getState()) && !status.equals(States.IN_PROGRESS)) {
                    continue;
                }

                epic.setState(States.IN_PROGRESS);
                return;
            }

            if (status == null) {
                status = States.NEW;
            }

            epic.setState(status);
        }
    }

    // Получение списка подзадач определенного эпика
    @Override
    public List<Integer> getEpicSubtasks(Integer id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            return new ArrayList<>(epic.getSubtasks());
        } else {
            return null;
        }
    }

    @Override
    public List<Subtask> getListSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            inMemoryHistoryManager.remove(subtask.getId());
        }

        subtasks.clear();

        // Обнулим списки и пересчитаем статусы у всех эпиков
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            updateStateEpic(epic);
        }
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            inMemoryHistoryManager.add(subtask);
            return subtask;
        } else {
            return null;
        }
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) return;

        int epicId = subtask.getEpicId();
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);

        // Добавим подзадачу в эпик
        Epic epic = epics.get(epicId);
        epic.getSubtasks().add(subtask.getId());

        // пересчтиаем статус у Эпика
        updateStateEpic(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null && !subtasks.containsValue(subtask)) return;

        subtasks.put(subtask.getId(), subtask);

        // пересчтиаем статус у Эпика
        Epic epic = getEpicById(subtask.getEpicId());
        updateStateEpic(epic);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            Epic epic = epics.get(subtask.getEpicId());

            inMemoryHistoryManager.remove(id);
            subtasks.remove(id);

            //Удалим подзадачу из списка эпика
            epic.getSubtasks().remove(id);

            // пересчтиаем статус у Эпика
            updateStateEpic(epic);
        }
    }
}