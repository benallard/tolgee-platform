import { useState } from 'react';
import { useHistory } from 'react-router-dom';
import { T } from '@tolgee/react';
import { container } from 'tsyringe';

import { LINKS } from 'tg.constants/links';
import { useGlobalActions } from 'tg.globalContext/GlobalContext';
import { useApiMutation } from 'tg.service/http/useQueryApi';
import { MessageService } from 'tg.service/MessageService';
import { confirmation } from 'tg.hooks/confirmation';

const messaging = container.resolve(MessageService);

export const useLeaveProject = () => {
  const history = useHistory();
  const { refetchInitialData } = useGlobalActions();
  const [isLeaving, setIsLeaving] = useState(false);

  const leaveLoadable = useApiMutation({
    url: '/v2/projects/{projectId}/leave',
    method: 'put',
    options: {
      onSuccess() {
        refetchInitialData();
        messaging.success(<T keyName="project_successfully_left" />);
        history.push(LINKS.PROJECTS.build());
      },
      onError(e) {
        switch (e.code) {
          case 'cannot_leave_project_with_organization_role':
            messaging.error(
              <T keyName="cannot_leave_project_with_organization_role_error_message" />
            );
            break;
          default:
            e.handleError?.();
        }
      },
      onSettled() {
        setIsLeaving(false);
      },
    },
  });

  const leave = (projectName: string, projectId: number) => {
    confirmation({
      title: <T keyName="leave_project_confirmation_title" />,
      message: <T keyName="leave_project_confirmation_message" />,
      hardModeText: projectName.toUpperCase(),
      onConfirm() {
        setIsLeaving(true);
        leaveLoadable.mutate({ path: { projectId } });
      },
    });
  };
  return { leave, isLeaving };
};
