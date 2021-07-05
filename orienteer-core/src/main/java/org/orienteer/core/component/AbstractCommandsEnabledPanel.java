package org.orienteer.core.component;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.command.Command;

import com.google.common.reflect.TypeToken;

/**
 * Panel with Commands on top
 *
 * @param <T> the type of model object
 */
public abstract class AbstractCommandsEnabledPanel<T> extends GenericPanel<T>  implements ICommandsSupportComponent<T> {

	private transient TypeToken<T> typeToken;
	protected final Form<T> form;
	private RepeatingView commands;
	
	
	public AbstractCommandsEnabledPanel(String id) {
		this(id, null);
	}
	
	public AbstractCommandsEnabledPanel(String id, IModel<T> model) {
		super(id, model);
		add(form = newForm("form", model));
		form.add(commands = new RepeatingView("commands"));
	}
	
	protected Form<T> newForm(String id, IModel<T> model) {
		return new Form<>(id, model);
	}

	@Override
	public AbstractCommandsEnabledPanel<T> addCommand(Command<T> command) {
		commands.add(command);
        return this;
	}

	@Override
	public AbstractCommandsEnabledPanel<T> removeCommand(Command<T> command) {
		commands.remove(command);
        return this;
	}

	@Override
	public String newCommandId() {
		return commands.newChildId();
	}
	
	@Override
	public TypeToken<T> getTypeToken() {
		if(typeToken==null) {
			typeToken = new TypeToken<T>(AbstractCommandsEnabledPanel.class) {};
		}
		return typeToken;
	}
}
