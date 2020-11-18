package org.orienteer.core.component.command;

import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexManagerAbstract;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.orienteer.core.component.table.OrienteerDataTable;
import ru.ydn.wicket.wicketorientdb.security.OSecurityHelper;
import ru.ydn.wicket.wicketorientdb.security.OrientPermission;
import ru.ydn.wicket.wicketorientdb.security.RequiredOrientResource;

import java.util.List;

/**
 * {@link Command} to delete {@link OIndex}
 */
@RequiredOrientResource(value = OSecurityHelper.SCHEMA, permissions=OrientPermission.DELETE)
public class DeleteOIndexCommand extends AbstractDeleteCommand<OIndex>
{
	private OIndexManagerAbstract indexManager;
	
	public DeleteOIndexCommand(OrienteerDataTable<OIndex, ?> table)
	{
		super(table);
	}
	
	@Override
	protected void performMultiAction(AjaxRequestTarget target, List<OIndex> objects) {
		getDatabaseSession().commit();
		super.performMultiAction(target, objects);
		getDatabaseSession().begin();
	}

	@Override
	protected void perfromSingleAction(AjaxRequestTarget target, OIndex object) {
		//object.delete(); //TODO: This doesn't work - might be make PR to OrientDB?
		getIndexManager().dropIndex(getDatabaseDocumentInternal(), object.getName());
	}
	
	protected OIndexManagerAbstract getIndexManager()
	{
		if(indexManager==null)
		{
			indexManager = getDatabaseDocumentInternal().getMetadata().getIndexManagerInternal();
		}
		return indexManager;
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		indexManager = null;
	}
	
	
	
}
