package org.orienteer.devutils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import org.apache.wicket.util.string.Strings;
import org.orienteer.core.OrienteerWebApplication;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.console.OConsoleDatabaseApp;
import com.orientechnologies.orient.core.command.script.OCommandScriptException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;

import ru.ydn.wicket.wicketconsole.IScriptContext;
import ru.ydn.wicket.wicketconsole.IScriptEngine;
import ru.ydn.wicket.wicketconsole.ScriptResult;

/**
 * Script engine to execute commands on OrientDB Console: {@link OConsoleDatabaseApp}
 */
public class ODBConsoleEngine implements IScriptEngine {

	@Override
	public String getName() {
		return ODBConsoleEngineFactory.ENGINE_NAME;
	}

	@Override
	public ScriptResult eval(String command, IScriptContext context) {
		ScriptResult result = new ScriptResult(ODBConsoleEngineFactory.ENGINE_NAME, command);
		if(!Strings.isEmpty(command)) {
			command = command.trim();
			String[] args = command.split("(?<=\") *(?=\")");
			final OConsoleDatabaseApp console = new OConsoleDatabaseApp(args) {
				@Override
				protected void onException(Throwable e) {
					result.setError(toError(e));
				}
			};
			console.set("verbose", "1");
//			OutputStream out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos);
			console.setOutput(out);
			result.start();
			try {
				console.setCurrentDatabase(OrienteerWebApplication.lookupApplication().getDatabaseDocumentInternal());
				console.run();
				result.setOut(baos.toString("UTF8"));
			} catch (Exception e) {
				result.setError(toError(e));
			} finally {
				result.finish();
			}
		}
		return result;
	}
	
	protected String toError(Throwable th) {
		return shouldBeShorted(th) ? th.getMessage() : Throwables.getStackTraceAsString(th);
	}
	
	protected boolean shouldBeShorted(Throwable e) {
		return e instanceof OCommandSQLParsingException || e instanceof OCommandScriptException || e instanceof OSecurityAccessException;
	}

}
