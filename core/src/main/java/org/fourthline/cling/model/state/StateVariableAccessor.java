/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.cling.model.state;

import org.fourthline.cling.model.Command;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;


/**
 * Reads the value of a state variable, given an instance that implements the service.
 *
 * TODO: The design of this is not final, not happy with the relationship between ActionExecutor and this.
 *
 * @author Christian Bauer
 */
public abstract class StateVariableAccessor {

	public class AccessCommand implements Command {
		Object result;
		Object serviceImpl;
		StateVariable<LocalService> stateVariable;

		public AccessCommand(StateVariable<LocalService> stateVariable, Object serviceImpl) {
			this.stateVariable = stateVariable;
			this.serviceImpl = serviceImpl;
		}

		public void execute(ServiceManager serviceManager) throws Exception {
			result = read(serviceImpl);
			if (stateVariable.getService().isStringConvertibleType(result)) {
				result = result.toString();
			}
		}
	}

	public StateVariableValue read(final StateVariable<LocalService> stateVariable, final Object serviceImpl) throws Exception {
		AccessCommand cmd = new AccessCommand(stateVariable, serviceImpl);
		stateVariable.getService().getManager().execute(cmd);
		return new StateVariableValue(stateVariable, cmd.result);
	}


	public abstract Class<?> getReturnType();

	// TODO: Especially this shouldn't be public
	public abstract Object read(Object serviceImpl) throws Exception;

	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ")";
	}
}
