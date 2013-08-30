// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.cleanup;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public abstract class TaskNode implements Callable<TaskResult> {
	private static final Logger l4j = Logger.getLogger(TaskNode.class);
	
	protected Set<TaskNode> parentTasks;
	protected SimpleDirectedGraph<TaskNode, DefaultEdge> graph;
	private boolean queued;
	
	public TaskNode( Set<TaskNode> parentTasks ) {
		this.parentTasks = parentTasks;
		if( parentTasks == null ) {
			this.parentTasks = new HashSet<TaskNode>();
		}
	}
	
	public TaskNode() {
		this.parentTasks = new HashSet<TaskNode>();
	}
	
	public void addParent( TaskNode parent ) {
		parentTasks.add( parent );
		if( graph != null ) {
			synchronized (graph) {
				graph.addEdge(parent, this);
			}
		}
	}
	
	public void addToGraph( SimpleDirectedGraph<TaskNode, DefaultEdge> graph) {
		this.graph = graph;
		
		synchronized( graph ) {
			graph.addVertex( this );
			for( TaskNode parent : parentTasks ) {
				try {
					graph.addEdge(parent, this);
				} catch( IllegalArgumentException e ) {
					// The parent task probably already completed.
					l4j.debug( "Failed to add edge, parent probably already completed: " + e );
				}
			}
		}
	}

	@Override
	public TaskResult call() throws Exception {
		if( graph == null ) {
			throw new IllegalStateException( "Task not in graph?" );
		}

		TaskResult result = null;
		try {
			result = execute();
		} catch( Exception e ) {
			result = new TaskResult( false );
		}
		
		// Completed.  Remove from graph.
		removeFromGraph();
		
		return result;
	}
	
	private void removeFromGraph() {
		if( graph == null ) {
			throw new IllegalStateException( "Task not in graph?" );
		}
		synchronized (graph) {
			graph.removeVertex(this);
		}
	}

	protected abstract TaskResult execute() throws Exception;

	public void setQueued(boolean queued) {
		this.queued = queued;
	}

	public boolean isQueued() {
		return queued;
	}

}
