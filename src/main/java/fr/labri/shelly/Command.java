package fr.labri.shelly;

public interface Command<C, M> extends Action<C, M>, Terminal<C, M> {
}
